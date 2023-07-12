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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBinding;
import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZone;
import com.obones.binding.airzone.internal.config.AirZoneBridgeConfiguration;
import com.obones.binding.airzone.internal.config.AirZoneThingConfiguration;
import com.obones.binding.airzone.internal.discovery.AirZoneDiscoveryService;
import com.obones.binding.airzone.internal.factory.AirZoneHandlerFactory;
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

    private boolean disposing = false;

    /*
     * **************************************
     * ***** Default visibility Objects *****
     */

    public Localization localization;

    /**
     * Information retrieved by {@link AirZoneBinding#AirZoneBinding}.
     */
    private AirZoneBridgeConfiguration airZoneBridgeConfiguration = new AirZoneBridgeConfiguration();

    private AirZoneApiManager apiManager = new AirZoneApiManager(airZoneBridgeConfiguration);

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
    @SuppressWarnings("null") // unexplained warnings on ThingTypeUID constructor
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
            }

            /*
            /*
             * finally clean up everything else
             */
            logger.trace("disposeSchedulerJob(): shut down JSON connection interface.");
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

        // do not use InetAddress.isReachable, the AirZone server does not respond to ping
        try (Socket soc = new Socket())
        {
            soc.connect(new InetSocketAddress(airZoneBridgeConfiguration.ipAddress, airZoneBridgeConfiguration.tcpPort), airZoneBridgeConfiguration.timeoutMsecs);
        } catch (IOException ex) {
            logger.error("bridgeParamsUpdated(): Bridge ip address {}:{} not reachable with {} timeout: {}.", airZoneBridgeConfiguration.ipAddress, airZoneBridgeConfiguration.tcpPort, airZoneBridgeConfiguration.timeoutMsecs, ex.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        resetProperties();

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

        apiManager.fetchStatus();

        doDiscovery();

        refreshProperties();

        syncChannelsWithProducts();

        logger.debug("refreshSchedulerJob() initiated by {} finished cycle {}.", Thread.currentThread(),
                refreshCounter);
        refreshCounter++;
    }

    @SuppressWarnings("null") // unexplainable warning on discoveryService despite null check right before the call
    private void doDiscovery()
    {
        if (discoveryService != null) {
            discoveryService.discoverZones(apiManager.getLatestResponse(), getThing().getUID());
        }
    }

    /**
     * In case of recognized changes in the real world, the method will
     * update the corresponding states via openHAB event bus.
     */
    private void syncChannelsWithProducts() {
        for (Thing thing : getThing().getThings()) {
            AirZoneThingConfiguration config = thing.getConfiguration().as(AirZoneThingConfiguration.class);
            AirZoneHvacZone zone = apiManager.getZone(config.systemId, config.zoneId);

            if (zone != null) {
                AirZoneThingHandler thingHandler = (AirZoneThingHandler) thing.getHandler();
                if (thingHandler != null) {
                    thingHandler.refreshProperties(thing, zone);

                    Set<ChannelUID> channelUIDs = new HashSet<>();
                    for (Channel channel : thing.getChannels()) {
                        ChannelUID uid = channel.getUID();
                        if (isLinked(uid)) 
                            channelUIDs.add(uid);
                    }

                    if (!channelUIDs.isEmpty()) {
                        //logger.warn("Some channels are linked");
                        for (ChannelUID uid : channelUIDs) {
                            thingHandler.refreshChannel(thing, uid, zone);
                        }
                    }
                }
            }
        }
        logger.trace("syncChannelsWithProducts() done.");
    }

    private void resetProperties() {
        thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_MAC, null);
    }

    @SuppressWarnings("unused")  // really, we just set the value to null in the code above, so the code is definitely not dead...
    private void refreshProperties() {
        if (thing.getProperties().get(AirZoneBindingConstants.PROPERTY_BRIDGE_MAC) == null) {
            var props = apiManager.getServerProperties();

            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_MAC, props.getMac());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_WIFI_CHANNEL, ((Integer)props.getWifiChannel()).toString());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_WIFI_QUALITY, ((Integer)props.getWifiQuality()).toString());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_WIFI_RSSI, ((Integer)props.getWifiRssi()).toString());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_INTERFACE, props.getInterface());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_FIRMWARE, props.getFirmware());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_TYPE, props.getType());

            var apiVersion = apiManager.getApiVersion();
            thing.setProperty(AirZoneBindingConstants.PROPERTY_BRIDGE_API_VERSION, apiVersion);
        }
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
            // Threads.findDeadlocked();
        }

        if (airZoneBridgeConfiguration.hasChanged) {
            logger.trace("handleCommandCommsJob(): work on updated bridge configuration parameters.");
            bridgeParamsUpdated();
        }

        syncChannelsWithProducts();

        if (command instanceof RefreshType) {
            // The bridge has no channels to refresh
        } else {
            // The bridge has no channels to handle a command for
        }

        updateStatus(ThingStatus.ONLINE);

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
