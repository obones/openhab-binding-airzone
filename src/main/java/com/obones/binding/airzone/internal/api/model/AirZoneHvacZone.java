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

import org.eclipse.jdt.annotation.Nullable;

public class AirZoneHvacZone {
    /* spell-checker:disable */
    private int systemID;
    private int zoneID;
    private String name = "";
    private int thermos_type;
    private String thermos_firmware = "";
    private int thermos_radio;
    private int on;
    private int double_sp;
    private double coolsetpoint;
    @SuppressWarnings("unused")
    private double coolmaxtemp;
    @SuppressWarnings("unused")
    private double coolmintemp;
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
    private double temp_step;
    private int @Nullable [] modes = {};
    private int mode;
    private int speeds;
    private int speed;
    private int coldStage;
    private int heatStage;
    private int coldStages;
    private int heatStages;
    private double humidity;
    private int units;
    private AirZoneError[] errors = {};
    private @Nullable Integer air_demand;
    private @Nullable Integer floor_demand;
    private @Nullable Integer cold_demand;
    private @Nullable Integer heat_demand;
    private @Nullable Integer aq_mode;
    private @Nullable Integer aq_quality;
    private @Nullable Double aq_thrlow;
    private @Nullable Double aq_thrhigh;
    @SuppressWarnings("unused")
    private double heatangle;
    @SuppressWarnings("unused")
    private double coldangle;
    private @Nullable Integer master_zoneID;
    private String eco_adapt = null;
    private int antifreeze;
    private @Nullable Integer slats_vswing;
    private @Nullable Integer slats_hswing;
    private @Nullable Integer slats_vertical;
    private @Nullable Integer slats_horizontal;

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

    public int getDoubleSetpoint() {
        return double_sp;
    }

    public double getCoolSetpoint() {
        return coolsetpoint;
    }

    public double getHeatSetpoint() {
        return heatsetpoint;
    }

    public double getSetpoint() {
        return setpoint;
    };

    public double getRoomTemp() {
        return roomTemp;
    };

    public int getSleep() {
        return sleep;
    };

    public double getTempStep() {
        return temp_step;
    }

    public int[] getModes() {
        if (modes == null)
            return new int[0];

        return modes;
    }

    public int getMode() {
        return mode;
    };

    public int[] getSpeeds() {
        if (speeds == null) {
            return new int[0];
        }
        int[] value = new int[speeds + 1];
        for (int i = 0; i < value.length; i++) {
            value[i] = i;
        }
        return value;
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

    public AirZoneError[] getErrors() {
        return errors;
    }

    public @Nullable Integer getAirDemand() {
        return air_demand;
    }

    public @Nullable Integer getFloorDemand() {
        return floor_demand;
    }
    
    public @Nullable Integer getColdDemand() {
        return cold_demand;
    }
    
    public @Nullable Integer getHeatDemand() {
        return heat_demand;
    }
    
    public @Nullable Integer getAirQuality() {
        return aq_quality;
    };

    public @Nullable Integer getAirQualityMode() {
        return aq_mode;
    }

    public @Nullable Double getAirQualityThresholdLow() {
        return aq_thrlow;
    };

    public @Nullable Double getAirQualityThresholdHigh() {
        return aq_thrhigh;
    };

    public @Nullable Integer getMasterZoneID() {
        return master_zoneID;
    };

    public @Nullable Integer getSlatsVSwing() {
        return slats_vswing;
    }

    public @Nullable Integer getSlatsHSwing() {
        return slats_hswing;
    }

    public @Nullable Integer getSlatsVertical() {
        return slats_vertical;
    }

    public @Nullable Integer getSlatsHorizontal() {
        return slats_horizontal;
    }

    public @Nullable String getEcoAdapt() {
        return eco_adapt;
    }

    public @Nullable Integer getAntiFreeze() {
        return antifreeze;
    }
    /* spell-checker:enable */
}
