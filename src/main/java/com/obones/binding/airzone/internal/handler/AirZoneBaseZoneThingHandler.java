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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZone;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZonePutRequestParameters;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneBaseZoneThingHandler} is the base class for zone things in this binding. It provides default
 * mechanisms for handling commands, which are sent via {@link AirZoneBridgeHandler} to one of the channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public abstract class AirZoneBaseZoneThingHandler extends AirZoneBaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneBaseZoneThingHandler.class);

    public AirZoneBaseZoneThingHandler(Thing thing, Localization localization) {
        super(thing, localization);
    }

    public abstract @Nullable AirZoneHvacZone getZone(AirZoneApiManager apiManager);

    protected @Nullable AirZoneHvacZone getZone(AirZoneBridgeHandler bridgeHandler) {
        return getZone(bridgeHandler.getApiManager());
    }

    protected void createOptionalChannels(AirZoneHvacZone zone, ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, AutoUpdatePolicy autoUpdatePolicy) {
        // create speed channel if it can be set to any value
        if (zone.getSpeeds().length > 0) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SPEED);

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_FAN_SPEED,
                    channelTypeUID, autoUpdatePolicy);
        }

        // create one or two setpoint channels depending on the zone capabilities
        ChannelTypeUID channelSetpointTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE);
        if (zone.getDoubleSetpoint() == 0) {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_SETPOINT,
                    channelSetpointTypeUID, autoUpdatePolicy);
        } else {
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_COOL_SETPOINT,
                    channelSetpointTypeUID, autoUpdatePolicy, "channel-type.airzone.zone.cool-setpoint.label",
                    "channel-type.airzone.zone.cool-setpoint.description");

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_HEAT_SETPOINT,
                    channelSetpointTypeUID, autoUpdatePolicy, "channel-type.airzone.zone.heat-setpoint.label",
                    "channel-type.airzone.zone.heat-setpoint.description");
        }

        if (zone.getAirQualityMode() != null) {
            ChannelTypeUID channelAirQualityModeTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_AIR_QUALITY_MODE);
            ChannelTypeUID channelAirQualityThresholdTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_AIR_QUALITY_THRESHOLD);

            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_MODE,
                    channelAirQualityModeTypeUID, autoUpdatePolicy);

            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_LOW_THRESHOLD, channelAirQualityThresholdTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.air-quality-low-threshold.label",
                    "channel-type.airzone.zone.air-quality-low-threshold.description");

            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_AIR_QUALITY_HIGH_THRESHOLD, channelAirQualityThresholdTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.air-quality-high-threshold.label",
                    "channel-type.airzone.zone.air-quality-high-threshold.description");
        }

        ChannelTypeUID channelSlatsSwingTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SLATS_SWING);
        if (zone.getSlatsVSwing() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_SWING, channelSlatsSwingTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.slats-vertical-swing.label",
                    "channel-type.airzone.zone.slats-vertical-swing.description");
        }

        if (zone.getSlatsHSwing() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_SWING, channelSlatsSwingTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.slats-horizontal-swing.label",
                    "channel-type.airzone.zone.slats-horizontal-swing.description");
        }

        ChannelTypeUID channelSlatsPositionTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                AirZoneBindingConstants.CHANNEL_TYPE_ZONE_SLATS_POSITION);
        if (zone.getSlatsHorizontal() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_HORIZONTAL_POSITION, channelSlatsPositionTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.slats-horizontal-position.label", null);
        }

        if (zone.getSlatsVertical() != null) {
            createOptionalChannel(callback, builder, thingUID,
                    AirZoneBindingConstants.CHANNEL_ZONE_SLATS_VERTICAL_POSITION, channelSlatsPositionTypeUID,
                    autoUpdatePolicy, "channel-type.airzone.zone.slats-horizontal-position.label", null);
        }

        if (zone.getEcoAdapt() != null) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_ECO_ADAPT);
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_ECO_ADAPT,
                    channelTypeUID, autoUpdatePolicy);
        }

        if (zone.getAntiFreeze() != null) {
            ChannelTypeUID channelTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_ZONE_ANTI_FREEZE);
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_ZONE_ANTI_FREEZE,
                    channelTypeUID, autoUpdatePolicy);
        }
    }

    protected void createOptionalChannels(AirZoneBridgeHandler bridgeHandler, AutoUpdatePolicy autoUpdatePolicy) {
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

        createOptionalChannels(zone, callback, builder, thingUID, autoUpdatePolicy);

        updateThing(builder.build());
    }

    @Override
    protected boolean handleActionCommand(ChannelUID channelUID, Command command, AirZoneApiManager apiManager) {
        logger.debug("handling action command {} for channel {}", command.toString(), channelUID.getAsString());
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
                logger.debug("Don't know how to handle action command {} for channel {}", command.toString(),
                        channelUID.getAsString());
                return false;
        }

        logger.debug("Done handling action command {} for channel {}", command.toString(), channelUID.getAsString());
        return true;
    }

    public abstract void refreshProperties(@Nullable AirZoneHvacZone zone);

    public abstract boolean refreshChannel(ChannelUID channelUID, @Nullable AirZoneHvacZone zone);

    @Override
    public boolean refreshChannel(ChannelUID channelUID, AirZoneApiManager apiManager) {
        AirZoneHvacZone zone = getZone(apiManager);

        return refreshChannel(channelUID, zone);
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

    public abstract AirZoneHvacZonePutRequestParameters getPutRequestParameters();
}
