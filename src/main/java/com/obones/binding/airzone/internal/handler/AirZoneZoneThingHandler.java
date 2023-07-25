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
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZone;
import com.obones.binding.airzone.internal.config.AirZoneZoneThingConfiguration;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneZoneThingHandler} is responsible for handling commands, which are
 * sent via {@link AirZoneBridgeHandler} to one of the channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneZoneThingHandler extends AirZoneBaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneZoneThingHandler.class);

    public AirZoneZoneThingHandler(Thing thing, Localization localization) {
        super(thing, localization);
    }

    @Override
    protected synchronized void initializeProperties(AirZoneBridgeHandler bridgeHandler) {
        AirZoneZoneThingConfiguration config = getConfigAs(AirZoneZoneThingConfiguration.class);

        thing.setProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID,
                AirZoneBridgeHandler.getZoneUniqueId(config.systemId, config.zoneId));
    }

    @Override
    protected synchronized void createOptionalChannels(AirZoneBridgeHandler bridgeHandler) {
        AirZoneHvacZone zone = getZone(bridgeHandler);
        if (zone == null) {
            return;
        }

        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.warn("createOptionalChannels: Could not get callback.");
            return;
        }

        ThingBuilder builder = editThing();
        ThingUID thingUID = thing.getUID();

        // create speed channel if it can be set to any value
        if (zone.getSpeeds().length > 0) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SPEED);

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED,
                    channelTypeUID);
        }

        // create one or two setpoint channels depending on the zone capabilities
        ChannelTypeUID channelSetpointTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE);
        if (zone.getDoubleSetpoint() == 0) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT,
                    channelSetpointTypeUID);
        } else {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_COOL_SETPOINT,
                    channelSetpointTypeUID, "channel-type.airzone.zone.coolSetpoint.label",
                    "channel-type.airzone.zone.coolSetpoint.description");

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_HEAT_SETPOINT,
                    channelSetpointTypeUID, "channel-type.airzone.zone.heatSetpoint.label",
                    "channel-type.airzone.zone.heatSetpoint.description");
        }

        ChannelTypeUID channelDemandTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_DEMAND);
        if (zone.getAirDemand() != null) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_AIR_DEMAND,
                    channelDemandTypeUID, "channel-type.airzone.zone.airDemand.label",
                    "channel-type.airzone.zone.airDemand.description");
        }
        if (zone.getFloorDemand() != null) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_FLOOR_DEMAND,
                    channelDemandTypeUID, "channel-type.airzone.zone.floorDemand.label",
                    "channel-type.airzone.zone.floorDemand.description");
        }
        if (zone.getColdDemand() != null) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_COLD_DEMAND,
                    channelDemandTypeUID, "channel-type.airzone.zone.coldDemand.label",
                    "channel-type.airzone.zone.coldDemand.description");
        }
        if (zone.getHeatDemand() != null) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_HEAT_DEMAND,
                    channelDemandTypeUID, "channel-type.airzone.zone.heatDemand.label",
                    "channel-type.airzone.zone.heatDemand.description");
        }

        if (zone.getAirQualityMode() != null) {
            ChannelTypeUID channelAirQualityModeTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_AIR_QUALITY_MODE);
            ChannelTypeUID channelAirQualityTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_AIR_QUALITY);
            ChannelTypeUID channelAirQualityThresholdTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_AIR_QUALITY_THRESHOLD);

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_MODE,
                    channelAirQualityModeTypeUID);

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY,
                    channelAirQualityTypeUID);

            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_LOW_THRESHOLD, channelAirQualityThresholdTypeUID,
                    "channel-type.airzone.zone.air-quality-low-threshold.label",
                    "channel-type.airzone.zone.air-quality-low-threshold.description");

            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_HIGH_THRESHOLD, channelAirQualityThresholdTypeUID,
                    "channel-type.airzone.zone.air-quality-high-threshold.label",
                    "channel-type.airzone.zone.air-quality-high-threshold.description");
        }

        ChannelTypeUID channelSlatsSwingTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SLATS_SWING);
        if (zone.getSlatsVSwing() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_SWING, channelSlatsSwingTypeUID,
                    "channel-type.airzone.zone.slats-vertical-swing.label",
                    "channel-type.airzone.zone.slats-vertical-swing.description");
        }

        if (zone.getSlatsHSwing() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_SWING, channelSlatsSwingTypeUID,
                    "channel-type.airzone.zone.slats-horizontal-swing.label",
                    "channel-type.airzone.zone.slats-horizontal-swing.description");
        }

        ChannelTypeUID channelSlatsPositionTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SLATS_POSITION);
        if (zone.getSlatsHorizontal() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_POSITION, channelSlatsPositionTypeUID,
                    "channel-type.airzone.zone.slats-horizontal-position.label", null);
        }

        if (zone.getSlatsVertical() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_POSITION, channelSlatsPositionTypeUID,
                    "channel-type.airzone.zone.slats-horizontal-position.label", null);
        }

        if (zone.getEcoAdapt() != null) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_ECO_ADAPT);
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_ECO_ADAPT,
                    channelTypeUID);
        }

        if (zone.getAntiFreeze() != null) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_ANTI_FREEZE);
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_ANTI_FREEZE,
                    channelTypeUID);
        }

        updateThing(builder.build());
    }

    @Override
    protected boolean handleActionCommand(ChannelUID channelUID, Command command, AirZoneApiManager apiManager) {
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

            case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_MODE:
                apiManager.setZoneAirQualityMode(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_LOW_THRESHOLD:
                apiManager.setZoneAirQualityLowThreshold(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_HIGH_THRESHOLD:
                apiManager.setZoneAirQualityHighThreshold(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_SWING:
                apiManager.setZoneVerticalSlatsSwing(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_SWING:
                apiManager.setZoneHorizontalSlatsSwing(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_POSITION:
                apiManager.setZoneVerticalSlatsPosition(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_POSITION:
                apiManager.setZoneHorizontalSlatsPosition(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_ECO_ADAPT:
                apiManager.setEcoAdapt(thing, command);
                break;

            case AirZoneBindingConstants.CHANNEL_ZONE_ANTI_FREEZE:
                apiManager.setAntiFreeze(thing, command);
                break;

            default:
                return false;
        }

        return true;
    }

    @Override
    public boolean refreshChannel(ChannelUID channelUID, AirZoneApiManager apiManager) {
        AirZoneHvacZone zone = getZone(apiManager);

        return refreshChannel(channelUID, zone);
    }

    public boolean refreshChannel(ChannelUID channelUID, @Nullable AirZoneHvacZone zone) {
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
                    newState = getErrorsToState(zone.getErrors());
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_AIR_DEMAND:
                    newState = zone.getAirDemand() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_FLOOR_DEMAND:
                    newState = zone.getFloorDemand() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_HEAT_DEMAND:
                    newState = zone.getHeatDemand() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_COLD_DEMAND:
                    newState = zone.getColdDemand() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_MODE:
                    newState = new StringType(
                            AirZoneBindingConstants.IntToAirQualityMode.get(zone.getAirQualityMode()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY:
                    newState = new StringType(AirZoneBindingConstants.IntToAirQuality.get(zone.getAirQuality()));
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_LOW_THRESHOLD:
                    @Nullable
                    Double thresholdLow = zone.getAirQualityThresholdLow();
                    if (thresholdLow != null)
                        newState = new QuantityType<>(thresholdLow, Units.PERCENT);
                    else
                        newState = UnDefType.NULL;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_HIGH_THRESHOLD:
                    @Nullable
                    Double thresholdHigh = zone.getAirQualityThresholdHigh();
                    if (thresholdHigh != null)
                        newState = new QuantityType<>(thresholdHigh, Units.PERCENT);
                    else
                        newState = UnDefType.NULL;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_SWING:
                    newState = zone.getSlatsVSwing() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_SWING:
                    newState = zone.getSlatsHSwing() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_POSITION:
                    @Nullable
                    Integer verticalPosition = zone.getSlatsVertical();
                    if (verticalPosition != null)
                        newState = new DecimalType(verticalPosition);
                    else
                        newState = UnDefType.NULL;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_POSITION:
                    @Nullable
                    Integer horizontalPosition = zone.getSlatsHorizontal();
                    if (horizontalPosition != null)
                        newState = new DecimalType(horizontalPosition);
                    else
                        newState = UnDefType.NULL;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_ECO_ADAPT:
                    @Nullable
                    String ecoAdapt = zone.getEcoAdapt();
                    if (ecoAdapt != null)
                        newState = new StringType(AirZoneBindingConstants.StringToEcoAdapt.get(ecoAdapt));
                    else
                        newState = UnDefType.NULL;
                    break;
                case AirZoneBindingConstants.CHANNEL_ZONE_ANTI_FREEZE:
                    newState = zone.getAntiFreeze() != 1 ? OnOffType.OFF : OnOffType.ON;
                    break;
            }

            if (newState != null) {
                updateState(channelUID, newState);
                return true;
            }
        }

        return false;
    }

    public void refreshProperties(@Nullable AirZoneHvacZone zone) {
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

    @Override
    public void refreshChannelsAndProperties(AirZoneApiManager apiManager, Set<ChannelUID> linkedChannelsUIDs) {
        AirZoneHvacZone zone = getZone(apiManager);

        if (zone != null) {
            refreshProperties(zone);

            for (ChannelUID uid : linkedChannelsUIDs) {
                refreshChannel(uid, zone);
            }
        }
    }

    @SuppressWarnings("unused") // the code in the else part is used but the IDE insist on saying it is dead...
    private List<@Nullable String> getAllowedModes(AirZoneHvacZone zone) {
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

    private @Nullable AirZoneHvacZone getZone() {
        Bridge bridge = getBridge();
        if (bridge == null)
            return null;

        return getZone(bridge);
    }

    private @Nullable AirZoneHvacZone getZone(Bridge bridge) {
        AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null)
            return null;

        return getZone(bridgeHandler);
    }

    private @Nullable AirZoneHvacZone getZone(AirZoneBridgeHandler bridgeHandler) {
        return getZone(bridgeHandler.getApiManager());
    }

    private @Nullable AirZoneHvacZone getZone(AirZoneApiManager apiManager) {
        if (!(thing.getHandler() instanceof AirZoneZoneThingHandler))
            return null;

        AirZoneZoneThingConfiguration config = getConfigAs(AirZoneZoneThingConfiguration.class);

        AirZoneHvacZone zone = apiManager.getZone(config.systemId, config.zoneId);
        if (zone == null)
            logger.warn("No zone data for {} - {}", config.systemId, config.zoneId);

        return zone;
    }

    public @Nullable StateDescriptionFragmentBuilder adjustChannelState(ChannelTypeUID channelTypeUID,
            StateDescriptionFragmentBuilder builder) {
        AirZoneHvacZone zone = getZone();
        if (zone == null)
            return null;

        switch (channelTypeUID.getId()) {
            case AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE:
                return builder.withStep(new BigDecimal(zone.getTempStep()));
            case AirZoneBindingConstants.CHANNEL_TYPE_ZONE_MODE:
                // only the master zone can change the mode of operation
                return builder.withReadOnly(zone.getZoneID() != zone.getMasterZoneID());
            default:
                return null;
        }
    }
}
