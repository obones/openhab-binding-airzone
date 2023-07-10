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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.AirZoneDetailedErrors;
import com.obones.binding.airzone.internal.api.model.AirZoneZone;
import com.obones.binding.airzone.internal.config.AirZoneThingConfiguration;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneThingHandler} is responsible for handling commands, which are
 * sent via {@link AirZoneBridgeHandler} to one of the channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneThingHandler extends BaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneThingHandler.class);
    private Localization localization;
    private static final Gson gson = new Gson();

    public AirZoneThingHandler(Thing thing, Localization localization) {
        super(thing);
        this.localization = localization;
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
            createOptionalChannels();
            initializeProperties();
            updateStatus(ThingStatus.ONLINE);
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

                thing.setProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID,
                        AirZoneBridgeHandler.getZoneUniqueId(config.systemId, config.zoneId));
            }
        }
        logger.trace("initializeProperties() done.");
    }
    private synchronized void createOptionalChannels() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                logger.warn("createOptionalChannels: Could not get bridge handler");
                return;
            }
            AirZoneThingConfiguration config = getConfigAs(AirZoneThingConfiguration.class);

            AirZoneZone zone = bridgeHandler.getApiManager().getZone(config.systemId, config.zoneId);
            if (zone == null) {
                logger.warn("createOptionalChannels: No zone data for {} - {}", config.systemId, config.zoneId);
                return;
            }

            ThingHandlerCallback callback = getCallback();
            if (callback == null) {
                logger.warn("createOptionalChannels: Could not get callback.");
                return;
            }

            ThingBuilder builder = editThing();

            // create speed channel if it can be set to any value
            if (zone.getSpeeds().length > 0) {
                ChannelUID channelUID = new ChannelUID(thing.getUID(), AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED);
                ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID, AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SPEED);
                ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);
                Channel channel = channelBuilder.build();

                builder.withChannel(channel);
            }

            // create one or two setpoint channels depending on the zone capabilities
            ChannelTypeUID channelSetpointTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID, AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE);
            if (zone.getDoubleSetpoint() == 0) {
                ChannelUID channelUID = new ChannelUID(thing.getUID(), AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT);
                ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelSetpointTypeUID);
                Channel channel = channelBuilder.build();
                builder.withChannel(channel);
            } else {
                ChannelUID channelCoolUID = new ChannelUID(thing.getUID(), AirZoneBindingConstants.CHANNEL_ZONE_COOL_SETPOINT);
                ChannelBuilder channelCoolBuilder = callback.createChannelBuilder(channelCoolUID, channelSetpointTypeUID);
                channelCoolBuilder.withLabel(localization.getText("channel-type.airzone.zone.coolSetpoint.label"));
                channelCoolBuilder.withDescription(localization.getText("channel-type.airzone.zone.coolSetpoint.description"));
                Channel channelCool = channelCoolBuilder.build();
                builder.withChannel(channelCool);

                ChannelUID channelHeatUID = new ChannelUID(thing.getUID(), AirZoneBindingConstants.CHANNEL_ZONE_HEAT_SETPOINT);
                ChannelBuilder channelHeatBuilder = callback.createChannelBuilder(channelHeatUID, channelSetpointTypeUID);
                channelHeatBuilder.withLabel(localization.getText("channel-type.airzone.zone.heatSetpoint.label"));
                channelHeatBuilder.withDescription(localization.getText("channel-type.airzone.zone.heatSetpoint.description"));
                Channel channelHeat = channelHeatBuilder.build();
                builder.withChannel(channelHeat);
            }

            // create "power" channel if provided by the api
            if ((zone.getPower() != null) && (zone.getMcConnected() != 0))
            {
                ChannelTypeUID channelPowerTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID, AirZoneBindingConstants.CHANNEL_TYPE_ZONE_POWER);
                ChannelUID channelUID = new ChannelUID(thing.getUID(), AirZoneBindingConstants.CHANNEL_ZONE_POWER);
                ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelPowerTypeUID);
                Channel channel = channelBuilder.build();
                builder.withChannel(channel);
            }

            updateThing(builder.build());
        }
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
                AirZoneApiManager apiManager = bridgeHandler.getApiManager();
                Thing thing = getThing();

                if (command instanceof RefreshType) {
                    if (!refreshChannel(thing, channelUID, apiManager))
                        bridgeHandler.handleCommand(channelUID, command);
                } else {
                    String channelId = channelUID.getId();
                    switch (channelId) {
                        case AirZoneBindingConstants.CHANNEL_ZONE_NAME:
                            apiManager.setZoneName(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_ON_OFF:
                            apiManager.setZoneOnOff(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT:
                            apiManager.setZoneSetPoint(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_COOL_SETPOINT:
                            apiManager.setZoneCoolSetPoint(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_SETPOINT:
                            apiManager.setZoneHeatSetPoint(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_MODE:
                            apiManager.setZoneMode(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                            apiManager.setZoneSpeed(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_COLD_STAGE:
                            apiManager.setZoneColdStage(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_STAGE:
                            apiManager.setZoneHeatStage(thing, command);
                            break;

                        case AirZoneBindingConstants.CHANNEL_ZONE_SLEEP:
                            apiManager.setZoneSleep(thing, command);
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
        switch (info.getStatus()) {
            case OFFLINE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                break;

            case ONLINE:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;

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
            Unit<Temperature> temperatureUnit = (zone.getUnits() == 0 ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT);

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
                    newState = new QuantityType<>(zone.getRoomTemp(), temperatureUnit);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HUMIDITY:
                    newState = new QuantityType<>(zone.getHumidity(), Units.PERCENT);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT:
                    newState = new QuantityType<>(zone.getSetpoint(), temperatureUnit);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_SETPOINT:
                    newState = new QuantityType<>(zone.getHeatSetpoint(), temperatureUnit);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_COOL_SETPOINT:
                    newState = new QuantityType<>(zone.getCoolSetpoint(), temperatureUnit);
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_MODE:
                    newState = new StringType(AirZoneBindingConstants.IntToZoneMode.get(zone.getMode()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED:
                    newState = new DecimalType(zone.getSpeed());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_STAGE:
                    newState = new StringType(AirZoneBindingConstants.IntToStage.get(zone.getHeatStage()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_COLD_STAGE:
                    newState = new StringType(AirZoneBindingConstants.IntToStage.get(zone.getColdStage()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SLEEP:
                    newState = new StringType(AirZoneBindingConstants.IntToSleep.get(zone.getSleep()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_ERRORS:
                    var errors = new ArrayList<String>();

                    for(var zoneError : zone.getErrors()) {
                        String systemValue = zoneError.getSystem();
                        String zoneValue = zoneError.getZone();

                        boolean isSystem = systemValue != null;
                        boolean isZone = zoneValue != null;

                        String originName = isSystem ? "System" : (isZone ? "Zone" : "unknown");
                        String errorCode = isSystem ? systemValue : (isZone ? zoneValue : "unexpected");

                        String errorMessage = originName + ": " + errorCode;

                        @Nullable 
                        String detailedErrorMessage = AirZoneDetailedErrors.getDetailedErrorMessage(errorCode, localization);
                        if (detailedErrorMessage != null)
                            errorMessage += " - " + detailedErrorMessage;

                        errors.add(errorMessage);
                    }

                    newState = new StringType(gson.toJson(errors.toArray()));
                    break;
            }

            if (newState != null) {
                updateState(channelUID, newState);
                return true;
            }
        }

        return false;
    }

    public void refreshProperties(Thing thing, @Nullable AirZoneZone zone) {
        if (zone != null) {
            int thermostatType = zone.getThermosType();
            String thermostatTypeDesc = String.format("Unknown thermostat type: %d", thermostatType);
            switch (thermostatType) {
                case 1:
                    thermostatTypeDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_TYPE_BLUEFACE;
                    break;
                case 2:
                    thermostatTypeDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_TYPE_BLUEFACE_ZERO;
                    break;
                case 3:
                    thermostatTypeDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_TYPE_LITE;
                    break;
                case 4:
                    thermostatTypeDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_TYPE_THINK;
                    break;
            }

            int thermostatRadio = zone.getThermosRadio();
            String thermostatRadioDesc = String.format("Unknown thermostat radio: %d", thermostatRadio);
            switch (thermostatRadio) {
                case 0:
                    thermostatRadioDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_RADIO_CABLE;
                    break;
                case 1:
                    thermostatRadioDesc = AirZoneBindingConstants.ZONE_THERMOSTAT_RADIO_RADIO;
                    break;
            }

            List<@Nullable String> allowedModes = getAllowedModes(zone);

            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_TYPE, thermostatTypeDesc);
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_FIRMWARE, zone.getThermosFirmware());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_THERMOS_RADIO, thermostatRadioDesc);
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_MASTER_ZONE_ID,
                    Integer.toString(zone.getMasterZoneID()));
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_AVAILABLE_MODES, allowedModes.toString());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_AVAILABLE_SPEEDS,
                    Arrays.toString(zone.getSpeeds()));
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_AVAILABLE_COLD_STAGES,
                    AirZoneBindingConstants.IntToStage.get(zone.getColdStages()));
            thing.setProperty(AirZoneBindingConstants.PROPERTY_ZONE_AVAILABLE_HEAT_STAGES,
                    AirZoneBindingConstants.IntToStage.get(zone.getHeatStages()));
        }
    }

    @SuppressWarnings("unused") // the code in the else part is definitely used but the IDE insist on saying it is dead...
    private List<@Nullable String> getAllowedModes(AirZoneZone zone) {
        List<@Nullable String> result = new ArrayList<@Nullable String>();
        for (int allowedMode : zone.getModes()) {
            @Nullable
            String modeName = AirZoneBindingConstants.IntToZoneMode.get(allowedMode);
            if (modeName != null)
                result.add(modeName);
            else
                result.add(Integer.toString(allowedMode));
        }

        return result;
    }

    public @Nullable StateDescriptionFragmentBuilder adjustChannelState(ChannelTypeUID channelTypeUID, StateDescriptionFragmentBuilder builder) {
        switch (channelTypeUID.getId())
        {
            case AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE:
                Bridge bridge = getBridge();
                if (bridge == null) {
                    return null;
                }

                AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
                if (bridgeHandler == null) {
                    return null;
                }

                AirZoneThingConfiguration config = getConfigAs(AirZoneThingConfiguration.class);

                AirZoneZone zone = bridgeHandler.getApiManager().getZone(config.systemId, config.zoneId);
                if (zone == null) {
                    return null;
                }

                return builder.withStep(new BigDecimal(zone.getTempStep()));
            default:
                return null;
        }
    }
}
