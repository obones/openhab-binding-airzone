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
package com.obones.binding.airzone.internal.bridge;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.handler.AirZoneBridgeHandler;

/**
 * 2nd Level I/O interface towards the <B>AirZone</B> bridge.
 * It provides methods for pre- and post-communication
 * as well as a common method for the real communication.
 * The following class access methods exist:
 * <UL>
 * <LI>{@link AirZoneBridge#bridgeCommunicate} as method for the common communication.</LI>
 * </UL>
 * <P>
 * Each protocol-specific implementation provides a publicly visible
 * set of supported protocols as variable {@link #supportedProtocols}.
 * As root of several inheritance levels it pre-defines an
 * interfacing method {@link AirZoneBridge#bridgeAPI} which
 * has to be implemented by any kind of protocol-specific
 * communication returning the appropriate base (1st) level
 * communication method as well as any other gateway
 * interaction with {@link #bridgeDirectCommunicate}.
 *
 * @author Olivier Sannier - Initial contribution.
 */
@NonNullByDefault
public class AirZoneBridge {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneBridge.class);

    /**
     * Handler to access global bridge instance methods
     *
     */
    protected AirZoneBridgeHandler bridgeInstance;

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Constructor.
     * <P>
     * Initializes the binding-wide instance for dealing with common information and
     * the AirZone bridge connectivity settings by preparing the configuration settings with help
     * by AirZoneBridgeConfiguration.
     *
     * @param bridgeInstance refers to the binding-wide instance for dealing with common information.
     */
    public AirZoneBridge(AirZoneBridgeHandler bridgeInstance) {
        logger.trace("AirZoneBridge(constructor,bridgeInstance={}) called.", bridgeInstance);
        this.bridgeInstance = bridgeInstance;
        logger.trace("AirZoneBridge(constructor) done.");
    }

    // Destructor methods

    /**
     * Destructor.
     * <P>
     * De-initializes the binding-wide instance.
     *
     */
    public void shutdown() {
        logger.trace("shutdown() called.");
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last communication.
     * <P>
     * If possible, it should be overwritten by protocol specific implementation.
     * </P>
     *
     * @return timestamp (default zero).
     */
    public long lastCommunication() {
        logger.trace("lastCommunication() returns zero.");
        return 0L;
    }

    /**
     * Returns the timestamp in milliseconds since Unix epoch
     * of last successful communication.
     * <P>
     * If possible, it should be overwritten by protocol specific implementation.
     * </P>
     *
     * @return timestamp (default zero).
     */
    public long lastSuccessfulCommunication() {
        logger.trace("lastSuccessfulCommunication() returns zero.");
        return 0L;
    }

    /**
     * Check is the last communication was a good one
     *
     * @return true if the last communication was a good one
     */
    public boolean lastCommunicationOk() {
        return lastCommunication() != 0 && lastSuccessfulCommunication() == lastCommunication();
    }
}
