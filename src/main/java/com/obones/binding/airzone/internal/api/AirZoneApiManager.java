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

import com.google.gson.Gson;
import com.obones.binding.airzone.internal.api.model.*;
import com.obones.binding.airzone.internal.config.AirZoneBridgeConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AirZoneApiManager} is responsible for the communication with the AirZone web server.
 * It implements the different HTTP API calls provided by the AirZone web server
 *
 * @author Olivier Sannier - Initial contribution
 */
public class AirZoneApiManager {
    private final Logger logger = LoggerFactory.getLogger(AirZoneApiManager.class);
    private static final Gson gson = new Gson();

    private class AirZoneZoneMap extends HashMap<Integer, AirZoneZone> {
        private Integer getKey(int systemId, int zoneId) {
            return 1000 * systemId + zoneId;  // doc says each values goes from 1 to 32 so multiplying by 1000 should be safe for a long time while being human readable.
        }

        public @Nullable AirZoneZone put(int systemId, int zoneId, AirZoneZone zone) {
            return super.put(getKey(systemId, zoneId), zone);
        }

        public @Nullable AirZoneZone get(int systemId, int zoneId) {
            return super.get(getKey(systemId, zoneId));
        }
    }

    @Nullable 
    private AirZoneResponse latestResponse = null;
    private AirZoneZoneMap latestZones = new AirZoneZoneMap();
    private AirZoneBridgeConfiguration airZoneBridgeConfiguration;

    @Activate
    public AirZoneApiManager(final @Reference AirZoneBridgeConfiguration givenAirZoneBridgeConfiguration) {
        airZoneBridgeConfiguration = givenAirZoneBridgeConfiguration;
    }

    public void fetchStatus() {
        try {
            String jsonResponse = executePostUrl("{\"systemID\":0,\"zoneID\":0}");

            jsonResponse = jsonResponse.replaceAll("^.+,\n", "");
            logger.trace("io() cleaned response {}.", jsonResponse);
            latestResponse = gson.fromJson(jsonResponse, AirZoneResponse.class);

            for (AirZoneSystem system : latestResponse.getSystems()) {
                for (AirZoneZone zone : system.getData()) {
                    latestZones.put(zone.getSystemID(), zone.getZoneID(), zone);
                }
            }
        } catch (IOException ioe) {
            logger.warn("exception {}", ioe.toString());
        }
    }

    public AirZoneResponse getLatestResponse() {
        if (latestResponse == null)
            fetchStatus();

        return latestResponse;
    }

    public AirZoneZone getZone(int systemId, int zoneId) {
        return latestZones.get(systemId, zoneId);
    }

    private String executePostUrl(String requestContent) throws IOException {
        return executeUrl("POST", requestContent);
    }

    private String executeUrl(String httpMethod, String requestContent) throws IOException {
        String url = "http://".concat(airZoneBridgeConfiguration.ipAddress).concat(":").concat(Integer.toString(airZoneBridgeConfiguration.tcpPort)).concat("/api/v1/hvac");
        Properties headerItems = new Properties();
        InputStream content = new ByteArrayInputStream(requestContent.getBytes(StandardCharsets.UTF_8));
        //logger.warn("calling {}", url);
        String jsonResponse = HttpUtil.executeUrl("POST", url, headerItems, content, "application/json",
                airZoneBridgeConfiguration.timeoutMsecs);
        if (jsonResponse == null)
            logger.warn("no json response");

        // Give the bridge some time to breathe
        logger.trace("io(): wait time {} msecs.", airZoneBridgeConfiguration.timeoutMsecs);
        try {
            Thread.sleep(airZoneBridgeConfiguration.timeoutMsecs);
        } catch (InterruptedException ie) {
            logger.trace("io() wait interrupted.");
        }

        return jsonResponse;
    }
}
