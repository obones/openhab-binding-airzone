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
public class AirZoneZoneThingConfiguration {
    public static final String SYSTEM_ID = "systemId";
    public static final String ZONE_ID = "zoneId";

    /*
     * Value to flag any changes towards the getter.
     */
    public boolean hasChanged = true;

    /*
     * Default values - should not be modified
     */
    public int systemId = 1;
    public int zoneId = 1;
}
