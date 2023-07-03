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

public class AirZoneZone {
    /* spell-checker:disable */
    private int systemID;
    private int zoneID;
    private String name = "";
    private int thermos_type;
    private String thermos_firmware = "";
    private int thermos_radio;
    private int on;
    @SuppressWarnings("unused")
    private double double_sp;
    @SuppressWarnings("unused")
    private double coolsetpoint;
    @SuppressWarnings("unused")
    private double coolmaxtemp;
    @SuppressWarnings("unused")
    private double coolmintemp;
    @SuppressWarnings("unused")
    private double heatsetpoint;
    @SuppressWarnings("unused")
    private double heatmaxtemp;
    @SuppressWarnings("unused")
    private double heatmintemp;
    @SuppressWarnings("unused")
    private double maxTemp;
    @SuppressWarnings("unused")
    private double minTemp;
    private double setpoint;
    private double roomTemp;
    private int sleep;
    @SuppressWarnings("unused")
    private double temp_step;
    private int[] modes = {};
    private int mode;
    private int[] speeds = {};
    private int speed;
    private int coldStage;
    private int heatStage;
    private int coldStages;
    private int heatStages;
    private double humidity;
    private int units;
    @SuppressWarnings("unused")
    private String[] errors = {};
    @SuppressWarnings("unused")
    private int air_demand;
    @SuppressWarnings("unused")
    private int floor_demand;
    @SuppressWarnings("unused")
    private int cold_demand;
    @SuppressWarnings("unused")
    private int heat_demand;
    @SuppressWarnings("unused")
    private int aq_mode;
    private double aq_quality;
    /*@SuppressWarnings("unused")
    private double aq_thrlow;
    @SuppressWarnings("unused")
    private double aq_thrhigh;
    @SuppressWarnings("unused")
    private double heatangle;
    @SuppressWarnings("unused")
    private double coldangle;*/
    private int master_zoneID;
    /*@SuppressWarnings("unused")
    private String eco_adapt = "";
    @SuppressWarnings("unused")
    private int antifreeze;*/

    public int getSystemID() {
        return systemID;
    };

    public int getZoneID() {
        return zoneID;
    };

    public String getName() {
        return name;
    };

    public int getThermosType() {
        return thermos_type;
    };

    public String getThermosFirmware() {
        return thermos_firmware;
    };

    public int getThermosRadio() {
        return thermos_radio;
    };

    public int getOn() {
        return on;
    };

    public double getSetpoint() {
        return setpoint;
    };

    public double getRoomTemp() {
        return roomTemp;
    };

    public int getSleep() {
        return sleep;
    };

    public int[] getModes() {
        return modes;
    }

    public int getMode() {
        return mode;
    };

    public int[] getSpeeds() {
        return speeds;
    }

    public int getSpeed() {
        return speed;
    };

    public int getColdStage() {
        return coldStage;
    };

    public int getHeatStage() {
        return heatStage;
    };

    public int getColdStages() {
        return coldStages;
    };

    public int getHeatStages() {
        return heatStages;
    };

    public double getHumidity() {
        return humidity;
    };

    public int getUnits() {
        return units;
    }

    public double getAqQuality() {
        return aq_quality;
    };

    public int getMasterZoneID() {
        return master_zoneID;
    };
    /* spell-checker:enable */
}
