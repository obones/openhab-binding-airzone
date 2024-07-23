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
package com.obones.binding.airzone.internal.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.api.model.*;
import com.obones.binding.airzone.internal.config.AirZoneBridgeConfiguration;
import com.obones.binding.airzone.internal.handler.AirZoneBaseZoneThingHandler;

/**
 * The {@link AirZoneApiManager} is responsible for the communication with the AirZone web server.
 * It implements the different HTTP API calls provided by the AirZone web server
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneApiManager {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneApiManager.class);
    private static final Gson gson = new Gson();

    private class AirZoneHvacZoneMap extends HashMap<Integer, AirZoneHvacZone> {
        public static final long serialVersionUID = 1L;

        private Integer getKey(int systemId, int zoneId) {
            return 1000 * systemId + zoneId; // doc says each values goes from 1 to 32 so multiplying by 1000 should be
                                             // safe for a long time while being human readable.
        }

        public @Nullable AirZoneHvacZone put(int systemId, int zoneId, AirZoneHvacZone zone) {
            return super.put(getKey(systemId, zoneId), zone);
        }

        public @Nullable AirZoneHvacZone get(int systemId, int zoneId) {
            return super.get(getKey(systemId, zoneId));
        }
    }

    @Nullable
    private AirZoneHvacResponse latestZonesResponse = null;
    private AirZoneHvacZoneMap latestZones = new AirZoneHvacZoneMap();
    @Nullable
    private AirZoneHvacSystemsResponse latestSystemsResponse = null;
    private Map<Integer, AirZoneHvacSystemInfo> latestSystems = new HashMap<>();
    private AirZoneBridgeConfiguration airZoneBridgeConfiguration;

    @Activate
    public AirZoneApiManager(final @Reference AirZoneBridgeConfiguration givenAirZoneBridgeConfiguration) {
        airZoneBridgeConfiguration = givenAirZoneBridgeConfiguration;
    }

    public void fetchStatus() {
        try {
            String jsonResponse = executeHvacPostUrl("{\"systemID\":0,\"zoneID\":0}");

            if (jsonResponse != null) {
                jsonResponse = jsonResponse.replaceAll("^.+,\n", "");
                logger.trace("io() cleaned response {}.", jsonResponse);
                latestZonesResponse = gson.fromJson(jsonResponse, AirZoneHvacResponse.class);

                fillLatestZones(latestZonesResponse);
            }

            jsonResponse = executeHvacPostUrl("{\"systemID\":127}");
            if (jsonResponse != null) {
                jsonResponse = jsonResponse.replaceAll("^.+,\n", "");
                latestSystemsResponse = gson.fromJson(jsonResponse, AirZoneHvacSystemsResponse.class);

                fillLatestSystems(latestSystemsResponse);
            }
        } catch (IOException ioe) {
            logger.warn("fetchStatus: exception {}", ioe.toString());
        }
    }

    public @Nullable AirZoneHvacResponse getLatestZonesResponse() {
        if (latestZonesResponse == null)
            fetchStatus();

        return latestZonesResponse;
    }

    public @Nullable AirZoneHvacSystemsResponse getLatestSystemsResponse() {
        if (latestSystemsResponse == null)
            fetchStatus();

        return latestSystemsResponse;
    }

    public @Nullable AirZoneHvacZone getZone(int systemId, int zoneId) {
        if (latestZonesResponse == null)
            fetchStatus();

        return latestZones.get(systemId, zoneId);
    }

    public @Nullable AirZoneHvacZone getMasterZone(int systemId) {
        if (latestZonesResponse == null)
            fetchStatus();

        for (var zone : latestZones.values()) {
            if ((zone.getSystemID() == systemId) && getIsMasterZone(zone))
                return zone;
        }

        return null;
    }

    public @Nullable AirZoneHvacZone getMasterZone(AirZoneHvacSystem system) {
        // find the zone that has the same Id as the master zone id
        for (var zone : system.getData()) {
            if (getIsMasterZone(zone))
                return zone;
        }

        return null;
    }

    public static boolean getIsMasterZone(@Nullable AirZoneHvacZone zone) {
        if (zone == null)
            return false;

        @Nullable
        Integer masterZoneID = zone.getMasterZoneID();

        if (masterZoneID != null)
            return masterZoneID == zone.getZoneID();

        // older systems do not provide the masterZoneId field, so we assume the master zone is the only one with modes
        return zone.getModes().length > 0;
    }

    public @Nullable AirZoneHvacSystemInfo getSystem(int systemId) {
        if (latestZonesResponse == null)
            fetchStatus();

        return latestSystems.get(systemId);
    }

    public @Nullable AirZoneWebServerResponse getServerProperties() {
        try {
            String jsonResponse = executePostUrl("webserver", "");

            if (jsonResponse != null) {
                jsonResponse = jsonResponse.replaceAll("^.+,\n", "");
                logger.trace("io() cleaned response {}.", jsonResponse);
                return gson.fromJson(jsonResponse, AirZoneWebServerResponse.class);
            }
        } catch (IOException ioe) {
            logger.warn("getServerProperties: exception {}", ioe.toString());
        }
        return null;
    }

    public @Nullable String getApiVersion() {
        try {
            String jsonResponse = executePostUrl("version", "");

            if (jsonResponse != null) {
                @Nullable
                AirZoneApiVersionResponse apiVersionResponse = gson.fromJson(jsonResponse,
                        AirZoneApiVersionResponse.class);
                if (apiVersionResponse != null)
                    return apiVersionResponse.getVersion();
            }
        } catch (IOException ioe) {
            logger.warn("getApiVersion: exception {}", ioe.toString());
        }
        return null;
    }

    private @Nullable AirZoneHvacZone getZone(Thing thing) {
        ThingHandler thingHandler = thing.getHandler();
        if (thingHandler instanceof AirZoneBaseZoneThingHandler) {
            return ((AirZoneBaseZoneThingHandler) thingHandler).getZone(this);
        } else {
            return null;
        }
    }

    public void setZoneOnOff(Thing thing, Command command) {
        setChannelValue(thing, "on", command);
    }

    public void setZoneSetPoint(Thing thing, Command command) {
        setChannelValue(thing, "setpoint", command);
    }

    public void setZoneCoolSetPoint(Thing thing, Command command) {
        setChannelValue(thing, "coolsetpoint", command);
    }

    public void setZoneHeatSetPoint(Thing thing, Command command) {
        setChannelValue(thing, "heatsetpoint", command);
    }

    public void setZoneName(Thing thing, Command command) {
        setChannelValue(thing, "name", command);
    }

    public void setZoneMode(Thing thing, Command command) {
        if (command instanceof StringType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                @Nullable
                Integer value = AirZoneBindingConstants.ZoneModeToInt.get(((StringType) command).toString());
                if (value != null) {
                    int[] allowedModes = zone.getModes();
                    Arrays.sort(allowedModes);
                    if (Arrays.binarySearch(allowedModes, value) >= 0) {
                        setChannelValue(thing, "mode", new DecimalType(value));
                    } else {
                        logger.warn("Unsupported mode {} for zone {}, allowed modes are {}", value, thing.getUID(),
                                allowedModes);
                    }
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only StringType command is supported on zone mode, received {}", command.getClass().getName());
        }
    }

    public void setZoneSpeed(Thing thing, Command command) {
        if (command instanceof DecimalType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                int value = ((DecimalType) command).intValue();
                int[] allowedSpeeds = zone.getSpeeds();
                Arrays.sort(allowedSpeeds);
                if (Arrays.binarySearch(allowedSpeeds, value) >= 0) {
                    setChannelValue(thing, "speed", command);
                } else {
                    logger.warn("Unsupported speed {} for zone {}, allowed speeds are {}", value, thing.getUID(),
                            allowedSpeeds);
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only DecimalType command is supported on zone speed, received {}",
                    command.getClass().getName());
        }
    }

    public void setZoneColdStage(Thing thing, Command command) {
        setZoneStage(thing, command, "cold");
    }

    public void setZoneHeatStage(Thing thing, Command command) {
        setZoneStage(thing, command, "heat");
    }

    public void setZoneSleep(Thing thing, Command command) {
        if (command instanceof StringType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                @Nullable
                Integer value = AirZoneBindingConstants.ZoneSleepToInt.get(((StringType) command).toString());
                if (value != null) {
                    setChannelValue(thing, "sleep", new DecimalType(value));
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only StringType command is supported on zone mode, received {}", command.getClass().getName());
        }
    }

    public void setZoneAirQualityMode(Thing thing, Command command) {
        if (command instanceof StringType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                @Nullable
                Integer value = AirZoneBindingConstants.ZoneAirQualityModeToInt.get(((StringType) command).toString());
                if (value != null) {
                    setChannelValue(thing, "aq_mode", new DecimalType(value));
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only StringType command is supported on zone air quality mode, received {}",
                    command.getClass().getName());
        }
    }

    public void setZoneAirQualityLowThreshold(Thing thing, Command command) {
        setChannelValue(thing, "aq_thrlow", command);
    }

    public void setZoneAirQualityHighThreshold(Thing thing, Command command) {
        setChannelValue(thing, "aq_thrhigh", command);
    }

    public void setZoneVerticalSlatsSwing(Thing thing, Command command) {
        setChannelValue(thing, "slats_vswing", command);
    }

    public void setZoneHorizontalSlatsSwing(Thing thing, Command command) {
        setChannelValue(thing, "slats_hswing", command);
    }

    public void setZoneVerticalSlatsPosition(Thing thing, Command command) {
        setChannelValue(thing, "slats_vertical", command);
    }

    public void setZoneHorizontalSlatsPosition(Thing thing, Command command) {
        setChannelValue(thing, "slats_horizontal", command);
    }

    public void setEcoAdapt(Thing thing, Command command) {
        if (command instanceof StringType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                @Nullable
                String value = AirZoneBindingConstants.EcoAdaptToString.get(((StringType) command).toString());
                if (value != null) {
                    setChannelValue(thing, "eco_adapt", new StringType(value));
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only StringType command is supported on zone eco adapt, received {}",
                    command.getClass().getName());
        }
    }

    public void setAntiFreeze(Thing thing, Command command) {
        setChannelValue(thing, "antifreeze", command);
    }

    private void setZoneStage(Thing thing, Command command, String prefix) {
        if (command instanceof StringType) {
            AirZoneHvacZone zone = getZone(thing);
            if (zone != null) {
                int allowedStages = (prefix == "cold" ? zone.getColdStages() : zone.getHeatStages());
                @Nullable
                Integer value = AirZoneBindingConstants.ZoneStageToInt.get(((StringType) command).toString());
                if (value != null) {
                    if (value == allowedStages) {
                        setChannelValue(thing, prefix + "stage", new DecimalType(value));
                    } else {
                        logger.warn("Unsupported {} stage {} for zone {}, allowed stages are {}", prefix, value,
                                thing.getUID(), allowedStages);
                    }
                }
            } else {
                logger.warn("No zone values for {}", thing.getUID());
            }
        } else {
            logger.warn("Only StringType command is supported on zone stage, received {}",
                    command.getClass().getName());
        }
    }

    private void fillLatestZones(@Nullable AirZoneHvacResponse latestResponse) {
        if (latestResponse != null) {
            for (AirZoneHvacSystem system : latestResponse.getSystems()) {
                for (AirZoneHvacZone zone : system.getData()) {
                    latestZones.put(zone.getSystemID(), zone.getZoneID(), zone);
                }
            }
        }
    }

    private void fillLatestSystems(@Nullable AirZoneHvacSystemsResponse latestSystemsResponse) {
        if (latestSystemsResponse != null) {
            var systems = latestSystemsResponse.getSystems();
            if (systems != null) {
                for (AirZoneHvacSystemInfo systemInfo : systems) {
                    latestSystems.put(systemInfo.getSystemID(), systemInfo);
                }
            }
        }
    }

    private void setChannelValue(Thing thing, String fieldName, Command command) {
        ThingHandler thingHandler = thing.getHandler();
        if (!(thingHandler instanceof AirZoneBaseZoneThingHandler))
            return;

        AirZoneHvacZonePutRequestParameters putRequestParameters = ((AirZoneBaseZoneThingHandler) thingHandler)
                .getPutRequestParameters();

        JsonElement json = gson.toJsonTree(new Object());
        json.getAsJsonObject().addProperty("systemID", putRequestParameters.getSystemID());
        json.getAsJsonObject().addProperty("zoneID", putRequestParameters.getZoneID());

        if (command instanceof Number) {
            json.getAsJsonObject().addProperty(fieldName, ((Number) command).doubleValue());
        } else if (command instanceof OnOffType) {
            json.getAsJsonObject().addProperty(fieldName, (command == OnOffType.ON) ? 1 : 0);
        } else {
            json.getAsJsonObject().addProperty(fieldName, command.toString());
        }

        String content = gson.toJson(json);
        if (content != null) {
            try {
                executeHvacPutUrl(content);
            } catch (IOException ioe) {
                logger.warn("setChannelValue: {} - exception {}", fieldName, ioe.toString());
            }
        }

        fetchStatus();
    }

    private @Nullable String executeHvacPostUrl(String requestContent) throws IOException {
        return executePostUrl("hvac", requestContent);
    }

    private @Nullable String executePostUrl(String resourceName, String requestContent) throws IOException {
        return executeUrl("POST", resourceName, requestContent);
    }

    private @Nullable String executeHvacPutUrl(String requestContent) throws IOException {
        return executePutUrl("hvac", requestContent);
    }

    private @Nullable String executePutUrl(String resourceName, String requestContent) throws IOException {
        return executeUrl("PUT", resourceName, requestContent);
    }

    private static final Object executeUrlLock = new Object();
    private static @Nullable Instant nextCallNotBefore = null;

    private @Nullable String executeUrl(String httpMethod, String resourceName, String requestContent)
            throws IOException {
        String url = "http://".concat(airZoneBridgeConfiguration.ipAddress).concat(":")
                .concat(Integer.toString(airZoneBridgeConfiguration.tcpPort)).concat("/api/v1/").concat(resourceName);
        Properties headerItems = new Properties();
        InputStream content = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));

        String jsonResponse = null;
        logger.trace("executeUrl - {}: trying to enter synchronized section", httpMethod);
        synchronized (executeUrlLock) {
            // Give the bridge some time to breathe, but only wait until the next allowed time has been reached
            // If no call was ever made, use a default value in the past so that the while loop below exits immediately.
            Instant effectiveNextCallNotBefore = Optional.ofNullable(nextCallNotBefore)
                    .orElse(Instant.now().minus(1, ChronoUnit.MINUTES));

            logger.trace("executeUrl - {}: Next call not before: {}", httpMethod, effectiveNextCallNotBefore);
            while ((Instant.now().isBefore(effectiveNextCallNotBefore))) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ie) {
                    logger.trace("io() wait interrupted.");
                }
            }
            logger.trace("executeUrl - {}: wait has ended, send HTTP request", httpMethod);

            jsonResponse = HttpUtil.executeUrl(httpMethod, url, headerItems, content, "application/json",
                    airZoneBridgeConfiguration.timeoutMsecs);

            nextCallNotBefore = Instant.now().plus(3, ChronoUnit.SECONDS);
        }
        logger.trace("executeUrl - {}: exited synchronized section", httpMethod);

        if (jsonResponse == null)
            logger.warn("no json response");

        return jsonResponse;
    }
}
