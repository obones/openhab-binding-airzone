// @formatter:off
/**
 * Copyright (c) 2023-2024 Olivier Sannier 
 ** See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, 
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.airzone.internal.handler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.AbstractUID;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBinding;
import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneItemType;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneZone;
import com.obones.binding.airzone.internal.bridge.AirZoneBridge;
import com.obones.binding.airzone.internal.config.AirZoneBridgeConfiguration;
import com.obones.binding.airzone.internal.config.AirZoneThingConfiguration;
import com.obones.binding.airzone.internal.discovery.AirZoneDiscoveryService;
import com.obones.binding.airzone.internal.factory.AirZoneHandlerFactory;
import com.obones.binding.airzone.internal.handler.utils.ThingProperty;
import com.obones.binding.airzone.internal.utils.Localization;

/**
 * <B>Common interaction with the </B><I>AirZone</I><B> bridge.</B>
 * <P>
 * It implements the communication between <B>OpenHAB</B> and the <I>AirZone</I> Bridge:
 * <UL>
 * <LI><B>OpenHAB</B> Event Bus &rarr; <I>AirZone</I> <B>bridge</B>
 * <P>
 * Sending commands and value updates.</LI>
 * </UL>
 * <UL>
 * <LI><I>AirZone</I> <B>bridge</B> &rarr; <B>OpenHAB</B>:
 * <P>
 * Retrieving information by sending a Refresh command.</LI>
 * </UL>
 * <P>
 * Entry point for this class is the method
 * {@link AirZoneBridgeHandler#handleCommand handleCommand}.
 *
 * @author Olivier Sannier - Initial contribution.
 */
@NonNullByDefault
public class AirZoneBridgeHandler extends BaseBridgeHandler /*implements AirZoneBridgeInstance, AirZoneBridgeProvider */{

    /*
     * timeout to ensure that the binding shutdown will not block and stall the shutdown of OH itself
     */
    private static final int COMMUNICATION_TASK_MAX_WAIT_SECS = 10;

    /*
     * a modifier string to avoid the (small) risk of other tasks (outside this binding) locking on the same ip address
     * Strings.intern() object
     *
     */
    private static final String LOCK_MODIFIER = "airzone.ipaddr.";

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneBridgeHandler.class);

    // Class internal

    /**
     * Scheduler for continuous refresh by scheduleWithFixedDelay.
     */
    private @Nullable ScheduledFuture<?> refreshSchedulerJob = null;

    /**
     * Counter of refresh invocations by {@link refreshSchedulerJob}.
     */
    private int refreshCounter = 0;

    /**
     * Dedicated task executor for the long-running bridge communication tasks.
     *
     * Note: there is no point in using multi threaded thread-pool here, since all the submitted (Runnable) tasks are
     * anyway forced to go through the same serial pipeline, because they all call the same class level "synchronized"
     * method to actually communicate with the AirZone bridge via its one single TCP socket connection
     */
    private @Nullable ExecutorService communicationsJobExecutor = null;
    private @Nullable NamedThreadFactory threadFactory = null;
    private @Nullable AirZoneDiscoveryService discoveryService = null;

    private AirZoneBridge myJsonBridge = new /*Json*/AirZoneBridge(this);
    private boolean disposing = false;

    /*
     * **************************************
     * ***** Default visibility Objects *****
     */

    public AirZoneBridge thisBridge =  myJsonBridge;
    public BridgeParameters bridgeParameters = new BridgeParameters();
    public Localization localization;

    /**
     * Mapping from ChannelUID to class Thing2AirZoneActuator, which return AirZone device information, probably cached.
     */
    //public final Map<ChannelUID, Thing2AirZoneActuator> channel2AirZoneActuator = new ConcurrentHashMap<>();

    /**
     * Information retrieved by {@link AirZoneBinding#AirZoneBinding}.
     */
    private AirZoneBridgeConfiguration airZoneBridgeConfiguration = new AirZoneBridgeConfiguration();

    private AirZoneApiManager apiManager = new AirZoneApiManager(airZoneBridgeConfiguration);


    private @NonNullByDefault({}) Duration offlineDelay = Duration.ofMinutes(5);
    private int initializeRetriesDone = 0;

    /*
     * ************************
     * ***** Constructors *****
     */

    public AirZoneBridgeHandler(final Bridge bridge, Localization localization) {
        super(bridge);
        logger.trace("AirZoneBridgeHandler(constructor with bridge={}, localization={}) called.", bridge, localization);
        this.localization = localization;
        logger.debug("Creating a AirZoneBridgeHandler for thing '{}'.", getThing().getUID());
    }

    // Private classes

    /**
     * <P>
     * Set of information retrieved from the bridge/gateway:
     * </P>
     * <UL>
     * <LI>{@link #actuators} - Already known actuators,</LI>
     * <LI>{@link #scenes} - Already on the gateway defined scenes,</LI>
     * <LI>{@link #gateway} - Current status of the gateway status,</LI>
     * <LI>{@link #firmware} - Information about the gateway firmware revision,</LI>
     * <LI>{@link #lanConfig} - Information about the gateway configuration,</LI>
     * <LI>{@link #wlanConfig} - Information about the gateway configuration.</LI>
     * </UL>
     */
    public class BridgeParameters {
        /** Information retrieved by {@link AirZoneBridgeActuators#getProducts} */
        //public AirZoneBridgeActuators actuators = new AirZoneBridgeActuators();

        /** Information retrieved by {@link com.obones.binding.airzone.internal.bridge.AirZoneBridgeScenes#getScenes} */
        //AirZoneBridgeScenes scenes = new AirZoneBridgeScenes();
    }

    public AirZoneApiManager getApiManager() {
        return apiManager;
    }

    // Private methods

    /**
     * Provide the ThingType for a given Channel.
     * <P>
     * Separated into this private method to deal with the deprecated method.
     * </P>
     *
     * @param channelUID for type {@link ChannelUID}.
     * @return thingTypeUID of type {@link ThingTypeUID}.
     */
    public ThingTypeUID thingTypeUIDOf(ChannelUID channelUID) {
        String[] segments = channelUID.getAsString().split(AbstractUID.SEPARATOR);
        if (segments.length > 1) {
            return new ThingTypeUID(segments[0], segments[1]);
        }
        logger.warn("thingTypeUIDOf({}) failed.", channelUID);
        return new ThingTypeUID(AirZoneBindingConstants.BINDING_ID, AirZoneBindingConstants.UNKNOWN_THING_TYPE_ID);
    }

    // Objects and Methods for interface AirZoneBridgeInstance

    /**
     * Information retrieved by ...
     */
    //@Override
    public AirZoneBridgeConfiguration airZoneBridgeConfiguration() {
        return airZoneBridgeConfiguration;
    };

    // Provisioning/Deprovisioning methods *****

    @Override
    public void initialize() {
        // set the thing status to UNKNOWN temporarily and let the background task decide the real status
        updateStatus(ThingStatus.UNKNOWN);

        // take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting initialization.");
            return;
        }

        logger.trace("initialize(): initialize bridge configuration parameters.");
        airZoneBridgeConfiguration = new AirZoneBinding(getConfigAs(AirZoneBridgeConfiguration.class)).checked();
        apiManager = new AirZoneApiManager(airZoneBridgeConfiguration);

        /*
         * When a binding call to the hub fails with a communication error, it will retry the call for a maximum of
         * airZoneBridgeConfiguration.retries times, where the interval between retry attempts increases on each attempt
         * calculated as airZoneBridgeConfiguration.refreshMSecs * 2^retry (i.e. 1, 2, 4, 8, 16, 32 etc.) so a complete
         * retry series takes (airZoneBridgeConfiguration.refreshMSecs * ((2^(airZoneBridgeConfiguration.retries + 1)) - 1)
         * milliseconds. So we have to let this full retry series to have been tried (and failed), before we consider
         * the thing to be actually offline.
         */
        offlineDelay = Duration.ofMillis(
                ((long) Math.pow(2, airZoneBridgeConfiguration.retries + 1) - 1) * airZoneBridgeConfiguration.refreshMSecs);

        initializeRetriesDone = 0;

        scheduler.execute(() -> {
            disposing = false;
            initializeSchedulerJob();
        });
    }

    /**
     * Various initialization actions to be executed on a background thread
     */
    private void initializeSchedulerJob() {
        /*
         * synchronize disposeSchedulerJob() and initializeSchedulerJob() based an IP address Strings.intern() object to
         * prevent overlap of initialization and disposal communications towards the same physical bridge
         */
        synchronized (LOCK_MODIFIER.concat(airZoneBridgeConfiguration.ipAddress).intern()) {
            logger.trace("initializeSchedulerJob(): adopt new bridge configuration parameters.");
            bridgeParamsUpdated();

            if ((thing.getStatus() == ThingStatus.OFFLINE)
                    && (thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                if (initializeRetriesDone <= airZoneBridgeConfiguration.retries) {
                    initializeRetriesDone++;
                    scheduler.schedule(() -> initializeSchedulerJob(),
                            ((long) Math.pow(2, initializeRetriesDone) * airZoneBridgeConfiguration.timeoutMsecs),
                            TimeUnit.MILLISECONDS);
                }
                return;
            }

            long mSecs = airZoneBridgeConfiguration.refreshMSecs;
            logger.trace("initializeSchedulerJob(): scheduling refresh at {} milliseconds.", mSecs);
            refreshSchedulerJob = scheduler.scheduleWithFixedDelay(() -> {
                refreshSchedulerJob();
            }, mSecs, mSecs, TimeUnit.MILLISECONDS);

            AirZoneHandlerFactory.refreshBindingInfo();

            if (logger.isDebugEnabled()) {
                logger.debug("AirZone Bridge '{}' is initialized.", getThing().getUID());
            }
        }
    }

    @Override
    public void dispose() {
        scheduler.submit(() -> {
            disposing = true;
            disposeSchedulerJob();
        });
    }

    /**
     * Various disposal actions to be executed on a background thread
     */
    private void disposeSchedulerJob() {
        /*
         * synchronize disposeSchedulerJob() and initializeSchedulerJob() based an IP address Strings.intern() object to
         * prevent overlap of initialization and disposal communications towards the same physical bridge
         */
        synchronized (LOCK_MODIFIER.concat(airZoneBridgeConfiguration.ipAddress).intern()) {
            /*
             * cancel the regular refresh polling job
             */
            ScheduledFuture<?> refreshSchedulerJob = this.refreshSchedulerJob;
            if (refreshSchedulerJob != null) {
                logger.trace("disposeSchedulerJob(): cancel the refresh polling job.");
                refreshSchedulerJob.cancel(false);
            }

            ExecutorService commsJobExecutor = this.communicationsJobExecutor;
            if (commsJobExecutor != null) {
                this.communicationsJobExecutor = null;
                logger.trace("disposeSchedulerJob(): cancel any other scheduled jobs.");
                /*
                 * remove un-started communication tasks from the execution queue; and stop accepting more tasks
                 */
                commsJobExecutor.shutdownNow();
                /*
                 * if the last bridge communication was OK, wait for already started task(s) to complete (so the bridge
                 * won't lock up); but to prevent stalling the OH shutdown process, time out after
                 * MAX_COMMUNICATION_TASK_WAIT_TIME_SECS
                 */
                if (thisBridge.lastCommunicationOk()) {
                    try {
                        if (!commsJobExecutor.awaitTermination(COMMUNICATION_TASK_MAX_WAIT_SECS, TimeUnit.SECONDS)) {
                            logger.warn("disposeSchedulerJob(): unexpected awaitTermination() timeout.");
                        }
                    } catch (InterruptedException e) {
                        logger.warn("disposeSchedulerJob(): unexpected exception awaitTermination() '{}'.",
                                e.getMessage());
                    }
                }
            }

            /*
             * if the last bridge communication was OK, deactivate HSM to prevent queueing more HSM events
             */
            /*if (thisBridge.lastCommunicationOk()
                    && (new AirZoneBridgeSetHouseStatusMonitor().modifyHSM(thisBridge, false))) {
                logger.trace("disposeSchedulerJob(): HSM deactivated.");
            }*/

            /*
             * finally clean up everything else
             */
            logger.trace("disposeSchedulerJob(): shut down JSON connection interface.");
            myJsonBridge.shutdown();
            AirZoneHandlerFactory.refreshBindingInfo();
            logger.debug("AirZone Bridge '{}' is shut down.", getThing().getUID());
        }
    }

    /**
     * NOTE: It takes care by calling {@link #handleCommand} with the REFRESH command, that every used channel is
     * initialized.
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            //channel2AirZoneActuator.put(channelUID, new Thing2AirZoneActuator(this, channelUID));
            logger.trace("channelLinked({}) refreshing channel value with help of handleCommand as Thing is online.",
                    channelUID.getAsString());
            handleCommand(channelUID, RefreshType.REFRESH);
        } else {
            logger.trace("channelLinked({}) doing nothing as Thing is not online.", channelUID.getAsString());
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.trace("channelUnlinked({}) called.", channelUID.getAsString());
    }

    public void setDiscoveryService(AirZoneDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    // Reconfiguration methods

    private void bridgeParamsUpdated() {
        logger.debug("bridgeParamsUpdated() called.");

        thisBridge = myJsonBridge;

        // do not use InetAddress.isReachable, the AirZone server does not respond to ping
        try (Socket soc = new Socket())
        {
            soc.connect(new InetSocketAddress(airZoneBridgeConfiguration.ipAddress, airZoneBridgeConfiguration.tcpPort), airZoneBridgeConfiguration.timeoutMsecs);
        } catch (IOException ex) {
            logger.error("bridgeParamsUpdated(): Bridge ip address {}:{} not reachable with {} timeout: {}.", airZoneBridgeConfiguration.ipAddress, airZoneBridgeConfiguration.tcpPort, airZoneBridgeConfiguration.timeoutMsecs, ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        airZoneBridgeConfiguration.hasChanged = false;
        logger.debug("AirZone airZoneBridge is online, now.");
        updateStatus(ThingStatus.ONLINE);
        logger.trace("bridgeParamsUpdated() successfully finished.");
    }

    // Continuous synchronization methods

    private synchronized void refreshSchedulerJob() {
        logger.debug("refreshSchedulerJob() initiated by {} starting cycle {}.", Thread.currentThread(),
                refreshCounter);
        logger.trace("refreshSchedulerJob(): processing of possible HSM messages.");


        if (discoveryService != null) {
            discoveryService.discoverZones(apiManager.getLatestResponse(), getThing().getUID());
        }

        syncChannelsWithProducts();

        logger.debug("refreshSchedulerJob() initiated by {} finished cycle {}.", Thread.currentThread(),
                refreshCounter);
        refreshCounter++;
    }

    /**
     * In case of recognized changes in the real world, the method will
     * update the corresponding states via openHAB event bus.
     */
    private void syncChannelsWithProducts() {
        for (Thing thing : getThing().getThings()) {
            AirZoneThingConfiguration config = thing.getConfiguration().as(AirZoneThingConfiguration.class);
            AirZoneZone zone = apiManager.getZone(config.systemId, config.zoneId);

            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_TYPE, Integer.toString(zone.getThermosType()));
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_FIRMWARE, zone.getThermosFirmware());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_RADIO, Integer.toString(zone.getThermosRadio()));
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_MASTER_ZONE_ID, Integer.toString(zone.getMasterZoneID()));

            Set<ChannelUID> channelUIDs = new HashSet<>();
            for (Channel channel : thing.getChannels()) {
                ChannelUID uid = channel.getUID();
                if (isLinked(uid)) 
                    channelUIDs.add(uid);
            }

            AirZoneThingHandler thingHandler = (AirZoneThingHandler) thing.getHandler();
            if (!channelUIDs.isEmpty() && thingHandler != null) {
                //logger.warn("Some channels are linked");
                for (ChannelUID uid : channelUIDs) {
                    thingHandler.refreshChannel(thing, uid, zone);
                }
            }
        }
        logger.trace("syncChannelsWithProducts() done.");
    }

    // Processing of openHAB events

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({}): command {} on channel {} will be scheduled.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommand({},{}) called.", channelUID.getAsString(), command);

        // Background execution of bridge related I/O
        submitCommunicationsJob(() -> {
            handleCommandCommsJob(channelUID, command);
        });
        logger.trace("handleCommand({}) done.", Thread.currentThread());
    }

    /**
     * Normally called by {@link #handleCommand} to handle a command for a given channel with possibly long execution
     * time.
     * <p>
     * <B>NOTE:</B> This method is to be called as separated thread to ensure proper openHAB framework in parallel.
     * <p>
     *
     * @param channelUID the {@link ChannelUID} of the channel to which the command was sent,
     * @param command the {@link Command}.
     */
    private synchronized void handleCommandCommsJob(ChannelUID channelUID, Command command) {
        logger.trace("handleCommandCommsJob({}): command {} on channel {}.", Thread.currentThread(), command,
                channelUID.getAsString());
        logger.debug("handleCommandCommsJob({},{}) called.", channelUID.getAsString(), command);

        /*
         * ===========================================================
         * Common part
         */

        if (airZoneBridgeConfiguration.isProtocolTraceEnabled) {
            //Threads.findDeadlocked();
        }

        String channelId = channelUID.getId();
        State newState = null;
        String itemName = channelUID.getAsString();
        AirZoneItemType itemType = AirZoneItemType.getByThingAndChannel(thingTypeUIDOf(channelUID), channelUID.getId());

        if (itemType == AirZoneItemType.UNKNOWN) {
            logger.warn("{} Cannot determine type of Channel {}, ignoring command {}.",
                    AirZoneBindingConstants.LOGGING_CONTACT, channelUID, command);
            logger.trace("handleCommandCommsJob() aborting.");
            return;
        }

        if (airZoneBridgeConfiguration.hasChanged) {
            logger.trace("handleCommandCommsJob(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();
        }

        syncChannelsWithProducts();

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommandCommsJob(): work on refresh.");
            if (!itemType.isReadable()) {
                logger.debug("handleCommandCommsJob(): received a Refresh command for a non-readable item.");
            } else {
                logger.trace("handleCommandCommsJob(): refreshing item {} (type {}).", itemName, itemType);
                try { // expecting an IllegalArgumentException for unknown AirZone device
                    switch (itemType) {
                        // Bridge channels
                        case BRIDGE_STATUS:
                            //newState = ChannelBridgeStatus.handleRefresh(channelUID, channelId, this);
                            break;
                        case BRIDGE_DOWNTIME:
                            newState = new DecimalType(
                                    thisBridge.lastCommunication() - thisBridge.lastSuccessfulCommunication());
                            break;

                        default:
                            logger.warn("{} Cannot handle REFRESH on channel {} as it is of type {}.",
                                    AirZoneBindingConstants.LOGGING_CONTACT, itemName, channelId);
                    }
                } catch (IllegalArgumentException e) {
                    logger.warn("Cannot handle REFRESH on channel {} as it isn't (yet) known to the bridge.", itemName);
                }
                if (newState != null) {
                    if (itemType.isChannel()) {
                        logger.debug("handleCommandCommsJob(): updating channel {} to {}.", channelUID, newState);
                        updateState(channelUID, newState);
                    } else if (itemType.isProperty()) {
                        // if property value is 'unknown', null it completely
                        String val = newState.toString();
                        if (AirZoneBindingConstants.UNKNOWN.equals(val)) {
                            val = null;
                        }
                        logger.debug("handleCommandCommsJob(): updating property {} to {}.", channelUID, val);
                        ThingProperty.setValue(this, itemType.getIdentifier(), val);
                    }
                } else {
                    logger.warn("handleCommandCommsJob({},{}): updating of item {} (type {}) failed.",
                            channelUID.getAsString(), command, itemName, itemType);
                }
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            logger.trace("handleCommandCommsJob(): working on item {} (type {}) with COMMAND {}.", itemName, itemType,
                    command);
            try { // expecting an IllegalArgumentException for unknown AirZone device
                switch (itemType) {
                    // Bridge channels
                    case BRIDGE_RELOAD:
                        if (command == OnOffType.ON) {
                            logger.trace("handleCommandCommsJob(): about to reload information from AirZone bridge.");
                            bridgeParamsUpdated();
                        } else {
                            logger.trace("handleCommandCommsJob(): ignoring OFF command.");
                        }
                        break;
                        
                    default:
                        logger.warn("{} Cannot handle command {} on channel {} (type {}).",
                                AirZoneBindingConstants.LOGGING_CONTACT, command, itemName, itemType);
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Cannot handle command on channel {} as it isn't (yet) known to the bridge.", itemName);
            }
        }

        Instant lastCommunication = Instant.ofEpochMilli(thisBridge.lastCommunication());
        Instant lastSuccessfulCommunication = Instant.ofEpochMilli(thisBridge.lastSuccessfulCommunication());
        boolean lastCommunicationSucceeded = lastSuccessfulCommunication.equals(lastCommunication);
        ThingStatus thingStatus = getThing().getStatus();

        if (lastCommunicationSucceeded) {
            if (thingStatus == ThingStatus.OFFLINE || thingStatus == ThingStatus.UNKNOWN) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            if ((thingStatus == ThingStatus.ONLINE || thingStatus == ThingStatus.UNKNOWN)
                    && lastSuccessfulCommunication.plus(offlineDelay).isBefore(lastCommunication)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        }

        ThingProperty.setValue(this, AirZoneBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_ATTEMPT,
                lastCommunication.toString());
        ThingProperty.setValue(this, AirZoneBindingConstants.PROPERTY_BRIDGE_TIMESTAMP_SUCCESS,
                lastSuccessfulCommunication.toString());

        logger.trace("handleCommandCommsJob({}) done.", Thread.currentThread());
    }

    /**
     * If necessary initialize the communications job executor. Then check if the executor is shut down. And if it is
     * not shut down, then submit the given communications job for execution.
     */
    private void submitCommunicationsJob(Runnable communicationsJob) {
        ExecutorService commsJobExecutor = this.communicationsJobExecutor;
        if (commsJobExecutor == null) {
            commsJobExecutor = this.communicationsJobExecutor = Executors.newSingleThreadExecutor(getThreadFactory());
        }
        if (!commsJobExecutor.isShutdown()) {
            commsJobExecutor.execute(communicationsJob);
        }
    }

    /**
     * If necessary initialize the thread factory and return it
     *
     * @return the thread factory
     */
    public NamedThreadFactory getThreadFactory() {
        NamedThreadFactory threadFactory = this.threadFactory;
        if (threadFactory == null) {
            threadFactory = new NamedThreadFactory(getThing().getUID().getAsString());
        }
        return threadFactory;
    }

    /**
     * Indicates if the bridge thing is being disposed.
     *
     * @return true if the bridge thing is being disposed.
     */
    public boolean isDisposing() {
        return disposing;
    }

    public static String getZoneUniqueId(int systemId, int zoneId) {
        return "s" + systemId + "-z" + zoneId; 
    }
}
