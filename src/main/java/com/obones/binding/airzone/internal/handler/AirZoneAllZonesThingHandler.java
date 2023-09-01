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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZone;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZonePutRequestParameters;
import com.obones.binding.airzone.internal.config.AirZoneAllZonesThingConfiguration;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneAllZonesThingHandler} is responsible for handling commands, which are
 * sent via {@link AirZoneBridgeHandler} to control all zones at once.
 * All its channels are never auto updated as it is not possible to have a value for
 * all zones that would match users choices.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneAllZonesThingHandler extends AirZoneBaseZoneThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneAllZonesThingHandler.class);

    public AirZoneAllZonesThingHandler(Thing thing, Localization localization) {
        super(thing, localization);
    }

    @Override
    protected synchronized void initializeProperties(AirZoneBridgeHandler bridgeHandler) {
        AirZoneAllZonesThingConfiguration config = getConfigAs(AirZoneAllZonesThingConfiguration.class);

        thing.setProperty(AirZoneBindingProperties.PROPERTY_ALL_ZONES_UNIQUE_ID,
                AirZoneBridgeHandler.getZoneUniqueId(config.systemId, 0));
    }

    @Override
    protected void createOptionalChannels(AirZoneHvacZone zone, ThingHandlerCallback callback, ThingBuilder builder,
            ThingUID thingUID, AutoUpdatePolicy autoUpdatePolicy) {
        super.createOptionalChannels(zone, callback, builder, thingUID, autoUpdatePolicy);

        // No optional channels that are specific to the "All zones" handler
    }

    @Override
    protected synchronized void createOptionalChannels(AirZoneBridgeHandler bridgeHandler) {
        // We don't want our channels to ever auto update, effectively making them "write-only"
        super.createOptionalChannels(bridgeHandler, AutoUpdatePolicy.VETO);
    }

    @Override
    public boolean refreshChannel(ChannelUID channelUID, @Nullable AirZoneHvacZone zone) {
        // No channels will ever be updated as all our channels are "write only", but we return true
        // to prevent the ancestor code from forwarding the refresh request to the bridge handler
        return true;
    }

    @Override
    public void refreshProperties(@Nullable AirZoneHvacZone zone) {
        // No properties to update
    }

    @Override
    public @Nullable AirZoneHvacZone getZone(AirZoneApiManager apiManager) {
        if (!(thing.getHandler() instanceof AirZoneAllZonesThingHandler))
            return null;

        AirZoneAllZonesThingConfiguration config = getConfigAs(AirZoneAllZonesThingConfiguration.class);

        AirZoneHvacZone zone = apiManager.getMasterZone(config.systemId);
        if (zone == null)
            logger.warn("No master zone data for {}", config.systemId);

        return zone;
    }

    @Override
    public AirZoneHvacZonePutRequestParameters getPutRequestParameters() {
        AirZoneAllZonesThingConfiguration config = getConfigAs(AirZoneAllZonesThingConfiguration.class);

        return new AirZoneHvacZonePutRequestParameters(config.systemId, 0);
    }
}
