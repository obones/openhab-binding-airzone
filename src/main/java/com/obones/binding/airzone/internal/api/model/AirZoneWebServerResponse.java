// @formatter:off
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

import com.google.gson.annotations.SerializedName;

public class AirZoneWebServerResponse {
    private String mac;
    private int wifi_channel;
    private int wifi_quality;
    private int wifi_rssi;
    @SerializedName("interface")
    private String _interface;
    private String ws_firmware;
    private String ws_type;

    public String getMac() {
        return mac;
    }

    public int getWifiChannel() {
        return wifi_channel;
    }
    
    public int getWifiQuality() {
        return wifi_quality;
    }

    public int getWifiRssi() {
        return wifi_rssi;
    }
    
    public String getInterface() {
        return _interface;
    }

    public String getFirmware() {
        return ws_firmware;
    }

    public String getType() {
        return ws_type;
    }
}
