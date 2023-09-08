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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.AutoUpdatePolicy;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.AirZoneDetailedErrors;
import com.obones.binding.airzone.internal.api.model.AirZoneError;
import com.obones.binding.airzone.internal.utils.Localization;

/***
 * The{@link AirZoneBaseThingHandler} the base class for things in this binding. It provides default
 * mechanisms for handling commands, which are sent via {@link AirZoneBridgeHandler} to one of the channels.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public abstract class AirZoneBaseThingHandler extends BaseThingHandler {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneBaseThingHandler.class);
    private @NonNullByDefault({}) Set<String> channelsInActionCommand = Collections
            .synchronizedSet(new HashSet<String>());
    protected Localization localization;
    protected static final Gson gson = new Gson();

    public AirZoneBaseThingHandler(Thing thing, Localization localization) {
        super(thing);
        this.localization = localization;
        logger.trace("AirZoneBaseThingHandler(thing={},localization={}) constructor called.", thing, localization);
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

    protected synchronized void initializeProperties() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                initializeProperties(bridgeHandler);
            }
        }
        logger.trace("initializeProperties() done.");
    }

    protected abstract void initializeProperties(AirZoneBridgeHandler bridgeHandler);

    protected synchronized void createOptionalChannels() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            AirZoneBridgeHandler bridgeHandler = (AirZoneBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                logger.warn("createOptionalChannels: Could not get bridge handler");
                return;
            }

            createOptionalChannels(bridgeHandler);
        }
    }

    protected abstract void createOptionalChannels(AirZoneBridgeHandler bridgeHandler);

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, AutoUpdatePolicy autoUpdatePolicy,
            @Nullable String labelKey, @Nullable String descriptionKey) {
        ChannelUID channelUID = new ChannelUID(thing.getUID(), ChannelId);
        ChannelBuilder channelBuilder = callback.createChannelBuilder(channelUID, channelTypeUID);

        channelBuilder.withAutoUpdatePolicy(autoUpdatePolicy);
        if (labelKey != null)
            channelBuilder = channelBuilder.withLabel(localization.getText(labelKey));
        if (descriptionKey != null)
            channelBuilder = channelBuilder.withDescription(localization.getText(descriptionKey));

        Channel channel = channelBuilder.build();

        // Add channel (remove it beforehand to avoid duplicates, it's a no-op if it did not exist)
        return builder.withoutChannel(channelUID).withChannel(channel);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, AutoUpdatePolicy autoUpdatePolicy) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, autoUpdatePolicy, null,
                null);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID, @Nullable String labelKey,
            @Nullable String descriptionKey) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, AutoUpdatePolicy.DEFAULT,
                labelKey, descriptionKey);
    }

    protected ThingBuilder createOptionalChannel(ThingHandlerCallback callback, ThingBuilder builder, ThingUID thingUID,
            String ChannelId, ChannelTypeUID channelTypeUID) {
        return createOptionalChannel(callback, builder, thingUID, ChannelId, channelTypeUID, null, null);
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

                boolean commandHandled = false;
                if (command instanceof RefreshType) {
                    commandHandled = refreshChannel(channelUID, apiManager);
                } else {
                    channelsInActionCommand.add(channelUID.getAsString());
                    try {
                        commandHandled = handleActionCommand(channelUID, command, apiManager);
                    } finally {
                        channelsInActionCommand.remove(channelUID.getAsString());
                    }
                }

                if (!commandHandled)
                    bridgeHandler.handleCommand(channelUID, command);
            }
        }
    }

    protected abstract boolean handleActionCommand(ChannelUID channelUID, Command command,
            AirZoneApiManager apiManager);

    public abstract boolean refreshChannel(ChannelUID channelUID, AirZoneApiManager apiManager);

    public abstract void refreshChannelsAndProperties(AirZoneApiManager apiManager, Set<ChannelUID> linkedChannelsUIDs);

    protected State getErrorsToState(AirZoneError @Nullable [] airZoneErrors) {
        var errors = new ArrayList<String>();

        if (airZoneErrors != null) {
            for (var airZoneError : airZoneErrors) {
                String systemValue = airZoneError.getSystem();
                String zoneValue = airZoneError.getZone();

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
        }

        return new StringType(gson.toJson(errors.toArray()));
    }

    protected boolean channelIsInActionCommand(ChannelUID channelUID) {
        return channelsInActionCommand.contains(channelUID.getAsString());
    }
}
