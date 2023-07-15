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
package com.obones.binding.airzone.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class AirZoneBridgeConfiguration {
    public static final String BRIDGE_IPADDRESS = "ipAddress";
    public static final String BRIDGE_TCPPORT = "tcpPort";
    public static final String BRIDGE_TIMEOUT_MSECS = "timeoutMsecs";
    public static final String BRIDGE_RETRIES = "retries";
    public static final String BRIDGE_REFRESH_MSECS = "refreshMsecs";

    /*
     * Value to flag any changes towards the getter.
     */
    public boolean hasChanged = true;

    /*
     * Default values - should not be modified
     */
    public String ipAddress = "192.168.1.1";
    public int tcpPort = 3000;
    public int timeoutMsecs = 1000; // one second
    public int retries = 5;
    public long refreshMSecs = 10000L; // 10 seconds
}
