/**
 * Copyright (c) 2023-2024 Olivier Sannier
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. 
 * If a copy of the MPL was not distributed with this file, 
 * you can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package com.obones.binding.airzone.internal.handler;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.utils.Localization;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.AirZoneItemType;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneZone;
import com.obones.binding.airzone.internal.config.AirZoneThingConfiguration;

/***
 * The{@link AirZoneThingHandler} is responsible for handling commands, which are
 * sent via {@link AirZoneBridgeHandler} to one of the channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneThingHandler extends BaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneThingHandler.class);

    public AirZoneThingHandler(Thing thing, Localization localization) {
        super(thing);
        logger.trace("AirZoneThingHandler(thing={},localization={}) constructor called.", thing, localization);
    }

    @Override
    public void initialize() {
        logger.trace("initialize() called.");
        Bridge thisBridge = getBridge();
        logger.debug("initialize(): Initializing thing {} in combination with bridge {}.", getThing().getUID(),
                thisBridge);
        if (thisBridge == null) {
            logger.trace("initialize() updating ThingStatus to OFFLINE/CONFIGURATION_PENDING.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        } else if (thisBridge.getStatus() == ThingStatus.ONLINE) {
            logger.trace("initialize() updating ThingStatus to ONLINE.");
            updateStatus(ThingStatus.ONLINE);
            initializeProperties();
        } else {
            logger.trace("initialize() updating ThingStatus to OFFLINE/BRIDGE_OFFLINE.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
        logger.trace("initialize() done.");
    }

    private synchronized void initializeProperties() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                AirZoneThingConfiguration config = getConfigAs(AirZoneThingConfiguration.class);

                thing.setProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID, AirZoneBridgeHandler.getZoneUniqueId(config.systemId, config.zoneId));
            }
        }
        logger.trace("initializeProperties() done.");
    }

    @Override
    public void dispose() {
        logger.trace("dispose() called.");
        super.dispose();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.trace("channelLinked({}) called.", channelUID.getAsString());

        if (thing.getStatus() == ThingStatus.ONLINE) {
            handleCommand(channelUID, RefreshType.REFRESH);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("handleCommand({},{}) initiated by {}.", channelUID.getAsString(), command,
                Thread.currentThread());
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.trace("handleCommand() nothing yet to do as there is no bridge available.");
        } else {
            BridgeHandler handler = bridge.getHandler();
            if (handler == null) {
                logger.trace("handleCommand() nothing yet to do as thing is not initialized.");
            } else {
                AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) handler;
                AirZoneItemType itemType = AirZoneItemType.getByThingAndChannel(bridgeHandler.thingTypeUIDOf(channelUID), channelUID.getId());
                AirZoneApiManager apiManager = bridgeHandler.getApiManager();
                Thing thing = getThing();

                if (command instanceof RefreshType) {
                    if (!refreshChannel(thing, channelUID, apiManager))
                       bridgeHandler.handleCommand(channelUID, command); 
                } else {
                    switch (itemType) {
                        case ZONE_NAME:
                            apiManager.setZoneName(thing, command);
                            break;

                        case ZONE_ON_OFF:
                            apiManager.setZoneOnOff(thing, command);
                            break;

                        case ZONE_SETPOINT:
                            apiManager.setZoneSetPoint(thing, command);
                            break;
                            
                        case ZONE_MODE:
                            break;
                            
                        case ZONE_FAN_SPEED:
                            break;
                            
                        default:
                            handler.handleCommand(channelUID, command);
                    }
                }
            }
        }
        logger.trace("handleCommand() done.");
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (isInitialized()) { // prevents change of address
            validateConfigurationParameters(configurationParameters);
            Configuration configuration = editConfiguration();
            for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
                logger.trace("handleConfigurationUpdate(): found modified config entry {}.",
                        configurationParameter.getKey());
                configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
            }
            // persist new configuration and reinitialize handler
            dispose();
            updateConfiguration(configuration);
            initialize();
        } else {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo info) {
        switch (info.getStatus())
        {
            case OFFLINE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            case ONLINE:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            default:
                super.bridgeStatusChanged(info);
        }
    }

    public boolean refreshChannel(Thing thing, ChannelUID channelUID, AirZoneApiManager apiManager) {
        AirZoneThingConfiguration config = thing.getConfiguration().as(AirZoneThingConfiguration.class);
        AirZoneZone zone = apiManager.getZone(config.systemId, config.zoneId);

        return refreshChannel(thing, channelUID, zone);
    }

    public boolean refreshChannel(Thing thing, ChannelUID channelUID, @Nullable AirZoneZone zone) {
        if (zone != null) {
            State newState = null;
            String channelId = channelUID.getId();
            switch (channelId) {
                case AirZoneBindingConstants.CHANNEL_ZONE_NAME:
                    newState = new StringType(zone.getName());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_ON_OFF:
                    newState = (zone.getOn() != 0 ? OnOffType.ON : OnOffType.OFF);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_TEMPERATURE:
                    newState = new DecimalType(zone.getRoomTemp());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HUMIDITY:
                    newState = new DecimalType(zone.getHumidity());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT:
                    newState = new DecimalType(zone.getSetpoint());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_MODE:
                    newState = new DecimalType(zone.getMode());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                    newState = new DecimalType(zone.getSpeed());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_STAGE:
                    newState = new DecimalType(zone.getHeatStage());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_COLD_STAGE:
                    newState = new DecimalType(zone.getColdStage());
                    break;
            }

            if (newState != null) {
                updateState(channelUID, newState);
                return true;
            }
        }

        return false;
    }
}
