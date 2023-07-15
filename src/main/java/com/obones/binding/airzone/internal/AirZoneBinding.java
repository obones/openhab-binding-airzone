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
package com.obones.binding.airzone.internal;

import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.config.AirZoneBridgeConfiguration;

/***
 * <B>Class for AirZone binding which validates the bridge configuration parameters.</B>
 *
 * <ul>
 * <li>{@link #AirZoneBinding constructor}</li>
 * <li>{@link #checked }</li>
 * </ul>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneBinding extends AirZoneBridgeConfiguration {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(getClass());

    /***
     *** Startup methods
     ***/

    /**
     * Constructor
     *
     * initializes the interface towards the AirZone bridge. Furthermore, the checked configuration can be retrieved by
     * the method {@link #checked checked}.
     *
     * @param uncheckedConfiguration
     *            The configuration of type {@link AirZoneBridgeConfiguration}
     *            which shall be checked.
     */
    public AirZoneBinding(@Nullable AirZoneBridgeConfiguration uncheckedConfiguration) {
        logger.trace("AirZoneBinding(constructor) called.");
        if (logger.isTraceEnabled()) {
            for (Field field : AirZoneBridgeConfiguration.class.getFields()) {
                String fName = field.getName();
                if ((fName.length() > 0) && Character.isUpperCase(fName.charAt(0))) {
                    logger.trace("AirZoneBinding(): FYI: a potential configuration string is '{}'.", fName);
                }
            }
        }
        if (uncheckedConfiguration == null) {
            logger.debug("No configuration found, using default values.");
        } else {
            logger.trace("AirZoneBinding(): checking {}.", AirZoneBridgeConfiguration.BRIDGE_IPADDRESS);
            if (!uncheckedConfiguration.ipAddress.isBlank()) {
                this.ipAddress = uncheckedConfiguration.ipAddress;
            }
            logger.trace("AirZoneBinding(): checking {}.", AirZoneBridgeConfiguration.BRIDGE_TCPPORT);
            if ((uncheckedConfiguration.tcpPort > 0) && (uncheckedConfiguration.tcpPort <= 65535)) {
                this.tcpPort = uncheckedConfiguration.tcpPort;
            }
            logger.trace("AirZoneBinding(): checking {}.", AirZoneBridgeConfiguration.BRIDGE_TIMEOUT_MSECS);
            if ((uncheckedConfiguration.timeoutMsecs >= 500) && (uncheckedConfiguration.timeoutMsecs <= 5000)) {
                this.timeoutMsecs = uncheckedConfiguration.timeoutMsecs;
            }
            logger.trace("AirZoneBinding(): checking {}.", AirZoneBridgeConfiguration.BRIDGE_RETRIES);
            if ((uncheckedConfiguration.retries >= 0) && (uncheckedConfiguration.retries <= 10)) {
                this.retries = uncheckedConfiguration.retries;
            }
            logger.trace("AirZoneBinding(): checking {}.", AirZoneBridgeConfiguration.BRIDGE_REFRESH_MSECS);
            if ((uncheckedConfiguration.refreshMSecs >= 1000) && (uncheckedConfiguration.refreshMSecs <= 60000)) {
                this.refreshMSecs = uncheckedConfiguration.refreshMSecs;
            }

        }
        logger.trace("AirZoneBinding(constructor) done.");
    }

    /**
     * Access method returning a validated configuration.
     *
     * @return bridgeConfiguration of type {@link AirZoneBridgeConfiguration
     *         AirZoneBridgeConfiguration}.
     */
    public AirZoneBridgeConfiguration checked() {
        logger.trace("checked() called.");
        // @formatter:off
        logger.debug("{}Config[{}={},{}={},{}={},{}={},{}={},{}={},{}={},{}={},{}={}]",
                AirZoneBindingConstants.BINDING_ID,
                AirZoneBridgeConfiguration.BRIDGE_IPADDRESS, this.ipAddress,
                AirZoneBridgeConfiguration.BRIDGE_TCPPORT, tcpPort,
                AirZoneBridgeConfiguration.BRIDGE_TIMEOUT_MSECS, timeoutMsecs,
                AirZoneBridgeConfiguration.BRIDGE_RETRIES, retries,
                AirZoneBridgeConfiguration.BRIDGE_REFRESH_MSECS, refreshMSecs);
        // @formatter:off
        logger.trace("checked() done.");
        return this;
    }
}
