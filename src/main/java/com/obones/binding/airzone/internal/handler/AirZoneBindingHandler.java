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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.utils.Localization;
import com.obones.binding.airzone.internal.utils.ManifestInformation;

/***
 * The class is responsible for representing the overall status of the AirZone binding.
 * <P>
 * Beside the normal thing handling introduced by {@link BaseThingHandler}, it provides a method:
 * <ul>
 * <li>{@link #updateBindingState} to enable other classes to modify the number of activated AirZone bridges and
 * Things.</LI>
 * </UL>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneBindingHandler extends BaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneBindingHandler.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */
    private Thing thing;
    private Localization localization;
    private Integer currentNumberOfBridges = 0;
    private Integer currentNumberOfThings = 0;

    /*
     * ************************
     * ***** Constructors *****
     */

    public AirZoneBindingHandler(Thing thing, final Localization localization) {
        super(thing);
        this.thing = thing;
        this.localization = localization;
        logger.trace("AirZoneBindingHandler(constructor) called.");
    }

    /*
     * ***************************
     * ***** Private Methods *****
     */

    /**
     * Returns a human-readable representation of the binding state. This should help especially unexperienced user to
     * blossom up the introduction of the AirZone binding.
     *
     * @return bindingInformation of type {@link String}.
     */
    private String bridgeCountToString() {
        String information;
        switch (currentNumberOfBridges) {
            case 0:
                information = localization.getText("runtime.no-bridge");
                break;
            case 1:
                information = localization.getText("runtime.one-bridge");
                break;
            default:
                information = localization.getText("runtime.multiple-bridges");
                break;
        }
        return information;
    }

    /**
     * Modifies all information within openHAB to inform the user about the current state of this binding. That is:
     * <UL>
     * <LI>Update the properties about bundle version, number of bridges and things,</LI>
     * <LI>Usability of the binding in respect to defined bridges within the ThingStatus, and</LI>
     * <LI>Information of the binding state as channel value.</LI>
     * </UL>
     */
    private void updateVisibleInformation() {
        logger.trace("updateVisibleInformation(): updating properties.");
        thing.setProperty(AirZoneBindingProperties.PROPERTY_BINDING_BUNDLEVERSION,
                ManifestInformation.getBundleVersion());
        thing.setProperty(AirZoneBindingProperties.PROPERTY_BINDING_NOOFBRIDGES, currentNumberOfBridges.toString());
        thing.setProperty(AirZoneBindingProperties.PROPERTY_BINDING_NOOFTHINGS, currentNumberOfThings.toString());

        // BaseThingHandler is sensitive during initialization phase. Therefore, to avoid (wrong) warnings about:
        // "tried updating the thing status although the handler was already disposed."
        if (this.isInitialized()) {
            logger.trace("updateVisibleInformation(): updating thing status.");
            if (currentNumberOfBridges < 1) {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.CONFIGURATION_PENDING, bridgeCountToString());
            } else {
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, bridgeCountToString());
            }
            logger.trace("updateVisibleInformation(): updating all channels.");
            for (Channel channel : thing.getChannels()) {
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }
    }

    /*
     * *******************************************************************
     * ***** Objects and Methods for abstract class BaseThingHandler *****
     */

    @Override
    public void initialize() {
        logger.debug("initialize() called.");
        // The framework requires you to return from this method quickly.
        // Setting the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.UNKNOWN);
        // Take care of unusual situations...
        if (scheduler.isShutdown()) {
            logger.warn("initialize(): scheduler is shutdown, aborting the initialization of this bridge.");
            return;
        }
        logger.trace("initialize(): preparing background initialization task.");
        // Background initialization...
        scheduler.execute(() -> {
            logger.trace("initialize.scheduled(): Setting ThingStatus to ONLINE.");
            updateStatus(ThingStatus.ONLINE);
            updateVisibleInformation();
            logger.debug("AirZone Binding Info Element '{}' is initialized.", getThing().getUID());
        });
        logger.trace("initialize() done.");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) called.", channelUID.getAsString(), command);
        /*
         * ===========================================================
         * Common part
         */
        String channelId = channelUID.getId();
        State newState = null;
        String itemName = channelUID.getAsString();

        if (command instanceof RefreshType) {
            /*
             * ===========================================================
             * Refresh part
             */
            logger.trace("handleCommand(): refreshing item {}.", itemName);
            switch (channelId) {
                case AirZoneBindingConstants.CHANNEL_BINDING_INFORMATION:
                    newState = new StringType(bridgeCountToString());
                    break;
                default:
                    logger.trace("handleCommand(): cannot handle REFRESH on channel {} as it is of type {}.", itemName,
                            channelId);
            }
            if (newState != null) {
                logger.debug("handleCommand(): updating {} ({}) to {}.", itemName, channelUID, newState);
                updateState(channelUID, newState);
            } else {
                logger.info("handleCommand({},{}): updating of item {} failed.", channelUID.getAsString(), command,
                        itemName);
            }
        } else {
            /*
             * ===========================================================
             * Modification part
             */
            switch (channelId) {
                default:
                    logger.warn("handleCommand() cannot handle command {} on channel {}.", command, itemName);
            }
        }
        logger.trace("handleCommand() done.");
    }

    /*
     * **********************************
     * ***** (Other) Public Methods *****
     */

    /**
     * Update the information about bridges and things.
     * <P>
     * Provided for instrumentation of factory class to update this set of information.
     * </P>
     *
     * @param airZoneBridgeCount describing the number of initialized bridges.
     * @param airZoneThingCount describing the number of initialized things (in addition to Thing of type
     *            BindingInformation).
     */
    public void updateBindingState(Integer airZoneBridgeCount, Integer airZoneThingCount) {
        currentNumberOfBridges = airZoneBridgeCount;
        currentNumberOfThings = airZoneThingCount;
        updateVisibleInformation();
    }
}
