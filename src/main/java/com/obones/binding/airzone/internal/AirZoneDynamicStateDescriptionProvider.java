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
package com.obones.binding.airzone.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.obones.binding.airzone.internal.handler.AirZoneThingHandler;

/**
 * Dynamic channel state description provider.
 * Overrides the state description for channels that depend on device specific values
 *
 * @author Olivier Sannier - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, AirZoneDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class AirZoneDynamicStateDescriptionProvider implements DynamicStateDescriptionProvider {
    private final ThingRegistry thingRegistry;

    @Activate
    public AirZoneDynamicStateDescriptionProvider(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    public @Nullable ThingHandler findHandler(Channel channel) {
        Thing thing = thingRegistry.get(channel.getUID().getThingUID());
        if (thing == null) {
            return null;
        }

        return thing.getHandler();
    }

    @Override
    public @Nullable StateDescription getStateDescription(Channel channel,
            @Nullable StateDescription originalStateDescription, @Nullable Locale locale) {

        if (originalStateDescription == null) {
            return null;
        }

        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null || !AirZoneBindingConstants.BINDING_ID.equals(channelTypeUID.getBindingId())) {
            return null;
        }

        ThingHandler handler = findHandler(channel);
        if (handler == null) {
            return null;
        }

        if (handler instanceof AirZoneThingHandler) {
            AirZoneThingHandler thingHandler = (AirZoneThingHandler) handler;

            @Nullable
            StateDescriptionFragmentBuilder builder = StateDescriptionFragmentBuilder.create(originalStateDescription);
            builder = thingHandler.adjustChannelState(channelTypeUID, builder);
            if (builder != null) {
                StateDescription result = builder.build().toStateDescription();
                return result;
            }
        }

        return null;
    }
}
