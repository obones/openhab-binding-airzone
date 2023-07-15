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

public class AirZoneHvacSystemInfo {
    /* spell-checker:disable */
    private int systemID;
    private int mc_connected;
    private Double power = null;
    private String system_firmware;
    private int system_type;
    private String manufacturer;
    private AirZoneError[] errors = {};

    public int getSystemID() {
        return systemID;
    }

    public int getMc_connected() {
        return mc_connected;
    }

    public Double getPower() {
        return power;
    }

    public String getSystem_firmware() {
        return system_firmware;
    }

    public int getSystem_type() {
        return system_type;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public AirZoneError[] getErrors() {
        return errors;
    }
    /* spell-checker:enable */
}
