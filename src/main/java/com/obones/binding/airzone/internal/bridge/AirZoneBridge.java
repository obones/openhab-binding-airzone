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
public /*abstract*/ class AirZoneBridge {
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
     * Initializes a client/server communication towards <b>AirZone</b> bridge
     * based on the Basic I/O interface {@link AirZoneBridge} and parameters
     * passed as arguments (see below) and provided by AirZoneBridgeConfiguration.
     *
     * @param communication the intended communication,
     *            that is request and response interactions as well as appropriate URL definition.
     * @return true if communication was successful, and false otherwise.
     */
    /*private synchronized boolean bridgeCommunicate(BridgeCommunicationProtocol communication) {
        logger.trace("bridgeCommunicate({}) called.", communication.name());

        return bridgeDirectCommunicate(communication);
    }*/

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
     * Initializes a client/server communication towards <b>AirZone</b> airZoneBridge
     * based on the protocol-specific implementations with common parameters
     * passed as arguments (see below) and provided by AirZoneBridgeConfiguration.
     * <P>
     * For protocol-specific implementations this method has to be overwritten along the inheritance i.e.
     * with the protocol-specific class implementations.
     *
     * @param communication Structure of interface type {@link BridgeCommunicationProtocol} describing the
     *            intended communication.
     * @param useAuthentication boolean flag to decide whether to use authenticated communication.
     * @return <b>success</b> of type boolean which signals the success of the communication.
     */
    //protected abstract boolean bridgeDirectCommunicate(BridgeCommunicationProtocol communication);

    /**
     * Check is the last communication was a good one
     *
     * @return true if the last communication was a good one
     */
    public boolean lastCommunicationOk() {
        return lastCommunication() != 0 && lastSuccessfulCommunication() == lastCommunication();
    }
}
