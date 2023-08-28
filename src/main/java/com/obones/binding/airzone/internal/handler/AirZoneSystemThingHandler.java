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
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacSystemInfo;
import com.obones.binding.airzone.internal.config.AirZoneSystemThingConfiguration;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneSystemThingHandler} is responsible for handling commands, which are
 * sent via {@link AirZoneBridgeHandler} to one of the system channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneSystemThingHandler extends AirZoneBaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneZoneThingHandler.class);

    public AirZoneSystemThingHandler(Thing thing, Localization localization) {
        super(thing, localization);
    }

    @Override
    protected boolean handleActionCommand(ChannelUID channelUID, Command command, AirZoneApiManager apiManager) {
        String channelId = channelUID.getId();
        switch (channelId) {
        }

        return false;
    }

    @Override
    public boolean refreshChannel(ChannelUID channelUID, AirZoneApiManager apiManager) {
        AirZoneSystemThingConfiguration config = thing.getConfiguration().as(AirZoneSystemThingConfiguration.class);
        AirZoneHvacSystemInfo system = apiManager.getSystem(config.systemId);

        return refreshChannel(channelUID, system);
    }

    public boolean refreshChannel(ChannelUID channelUID, @Nullable AirZoneHvacSystemInfo system) {
        if (channelIsInActionCommand(channelUID))
            return true;

        if (system != null) {
            State newState = null;
            String channelId = channelUID.getId();
            switch (channelId) {
                case AirZoneBindingConstants.CHANNEL_SYSTEM_ERRORS:
                    newState = getErrorsToState(system.getErrors());
                    break;
            }

            if (newState != null) {
                updateState(channelUID, newState);
                return true;
            }
        }

        return false;
    }

    @Override
    protected synchronized void createOptionalChannels(AirZoneBridgeHandler bridgeHandler) {
        AirZoneSystemThingConfiguration config = getConfigAs(AirZoneSystemThingConfiguration.class);

        @Nullable
        AirZoneHvacSystemInfo system = bridgeHandler.getApiManager().getSystem(config.systemId);
        if (system == null) {
            logger.warn("createOptionalChannels: No system data for {}", config.systemId);
            return;
        }

        ThingHandlerCallback callback = getCallback();
        if (callback == null) {
            logger.warn("createOptionalChannels: Could not get callback.");
            return;
        }

        ThingBuilder builder = editThing();
        ThingUID thingUID = thing.getUID();

        @Nullable
        Double systemPower = system.getPower();
        if (systemPower != null) {
            ChannelTypeUID systemPowerTypeUID = new ChannelTypeUID(AirZoneBindingConstants.BINDING_ID,
                    AirZoneBindingConstants.CHANNEL_TYPE_SYSTEM_POWER);
            createOptionalChannel(callback, builder, thingUID, AirZoneBindingConstants.CHANNEL_SYSTEM_POWER,
                    systemPowerTypeUID);
        }
    }

    @Override
    protected synchronized void initializeProperties(AirZoneBridgeHandler bridgeHandler) {
        AirZoneSystemThingConfiguration config = getConfigAs(AirZoneSystemThingConfiguration.class);

        thing.setProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_UNIQUE_ID,
                AirZoneBridgeHandler.getSystemUniqueId(config.systemId));
    }

    @Override
    public void refreshChannelsAndProperties(AirZoneApiManager apiManager, Set<ChannelUID> linkedChannelsUIDs) {
        AirZoneSystemThingConfiguration config = getConfigAs(AirZoneSystemThingConfiguration.class);
        AirZoneHvacSystemInfo system = apiManager.getSystem(config.systemId);

        if (system != null) {
            refreshProperties(system);

            for (ChannelUID uid : linkedChannelsUIDs) {
                refreshChannel(uid, system);
            }
        }
    }

    public void refreshProperties(@Nullable AirZoneHvacSystemInfo system) {
        if (system != null) {
            int systemType = system.getSystem_type();
            String systemTypeDesc = String.format("Unknown system type: %d", systemType);
            switch (systemType) {
                case 1:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_C6;
                    break;
                case 2:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_AQUAGLASS;
                    break;
                case 3:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_DZK;
                    break;
                case 4:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_RADIANT;
                    break;
                case 5:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_C3;
                    break;
                case 6:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_ZBS;
                    break;
                case 7:
                    systemTypeDesc = AirZoneBindingConstants.SYSTEM_SYSTEM_TYPE_ZS6;
                    break;
            }

            thing.setProperty(AirZoneBindingConstants.PROPERTY_SYSTEM_SYSTEM_TYPE, systemTypeDesc);
            thing.setProperty(AirZoneBindingConstants.PROPERTY_SYSTEM_SYSTEM_FIRMWARE, system.getSystem_firmware());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_SYSTEM_MANUFACTURER, system.getManufacturer());
            thing.setProperty(AirZoneBindingConstants.PROPERTY_SYSTEM_METER_CONNECTED,
                    Boolean.toString((system.getMc_connected() != 0)));
        }
    }
}
