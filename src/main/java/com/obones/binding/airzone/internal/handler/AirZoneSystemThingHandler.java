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
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

                if (command instanceof RefreshType) {
                    if (!refreshChannel(channelUID, apiManager))
                        bridgeHandler.handleCommand(channelUID, command);
                } else {
                    String channelId = channelUID.getId();
                    switch (channelId) {
                    }
                }
            }
        }
    }

    @Override
    public boolean refreshChannel(ChannelUID channelUID, AirZoneApiManager apiManager) {
        AirZoneSystemThingConfiguration config = thing.getConfiguration().as(AirZoneSystemThingConfiguration.class);
        AirZoneHvacSystemInfo system = apiManager.getSystem(config.systemId);

        return refreshChannel(channelUID, system);
    }

    public boolean refreshChannel(ChannelUID channelUID, @Nullable AirZoneHvacSystemInfo system) {
        if (system != null) {
        }

        return false;
    }

    @Override
    protected synchronized void createOptionalChannels(AirZoneBridgeHandler bridgeHandler) {
    }

    @Override
    protected synchronized void initializeProperties(AirZoneBridgeHandler bridgeHandler) {
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
    }
}
