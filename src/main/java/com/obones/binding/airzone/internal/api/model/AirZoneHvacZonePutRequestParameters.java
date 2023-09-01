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
package com.obones.binding.airzone.internal.api.model;

public class AirZoneHvacZonePutRequestParameters {
    /* spell-checker:disable */
    private int systemID;
    private int zoneID;

    public AirZoneHvacZonePutRequestParameters(int systemID, int zoneID) {
        this.systemID = systemID;
        this.zoneID = zoneID;
    }

    public int getSystemID() {
        return systemID;
    };

    public int getZoneID() {
        return zoneID;
    };
}
