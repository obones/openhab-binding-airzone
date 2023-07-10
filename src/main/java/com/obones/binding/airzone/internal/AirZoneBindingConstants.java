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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirZoneBindingConstants} class defines common constants, which are
 * used across the whole binding.
 * <P>
 * This class contains the Thing identifications:
 * <UL>
 * <LI>{@link #THING_AIRZONE_BRIDGE} for the bridge itself,</LI>
 * <LI>{@link #THING_AIRZONE_ZONE} for the zones controlled by AirZone</LI>
 * </UL>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneBindingConstants {
    /** Basic binding identification. */
    public static final String BINDING_ID = "airzone";

    // Id of support bridge
    /**
     * The Thing identification of the binding.
     */
    private static final String THING_AIRZONE_BINDING = "binding";
    /**
     * The Thing identification of the <B>AirZone</B> bridge.
     */
    private static final String THING_AIRZONE_BRIDGE = "airzone";
    /**
     * The Thing identification of a zone defined on the <B>AirZone</B> bridge.
     */
    private static final String THING_AIRZONE_ZONE = "zone";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BINDING = new ThingTypeUID(BINDING_ID, THING_AIRZONE_BINDING);

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_AIRZONE_BRIDGE);

    // List of all Thing Type UIDs beyond the bridge(s)
    public static final ThingTypeUID THING_TYPE_AIRZONE_ZONE = new ThingTypeUID(BINDING_ID, THING_AIRZONE_ZONE);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BINDING = new HashSet<>(Arrays.asList(THING_TYPE_BINDING));
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));

    public static final Set<ThingTypeUID> SUPPORTED_THINGS_ITEMS = new HashSet<>(
            Arrays.asList(THING_TYPE_AIRZONE_ZONE));

    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Set<ThingTypeUID> DISCOVERABLE_THINGS = Set.of(THING_TYPE_AIRZONE_ZONE, THING_TYPE_BINDING,
            THING_TYPE_BRIDGE);

    // *** List of all Channel ids ***

    // List of all binding channel ids

    /** Channel identifier describing the current Binding State. */
    public static final String CHANNEL_BINDING_INFORMATION = "information";

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
    public static final String CHANNEL_BRIDGE_STATUS = "status";
    public static final String CHANNEL_BRIDGE_RELOAD = "reload";
    public static final String CHANNEL_BRIDGE_DOWNTIME = "downtime";
    public static final String CHANNEL_BRIDGE_DO_DETECTION = "doDetection";

    public static final String PROPERTY_BRIDGE_TIMESTAMP_SUCCESS = "connectionSuccess";
    public static final String PROPERTY_BRIDGE_TIMESTAMP_ATTEMPT = "connectionAttempt";

    public static final String PROPERTY_BRIDGE_SCENES = "zones";
    public static final String PROPERTY_BRIDGE_MAC = "mac";
    public static final String PROPERTY_BRIDGE_WIFI_CHANNEL = "wifiChannel";
    public static final String PROPERTY_BRIDGE_WIFI_QUALITY = "wifiQuality";
    public static final String PROPERTY_BRIDGE_WIFI_RSSI = "wifiRSSI";
    public static final String PROPERTY_BRIDGE_INTERFACE = "interface";
    public static final String PROPERTY_BRIDGE_FIRMWARE = "firmware";
    public static final String PROPERTY_BRIDGE_TYPE = "type";
    public static final String PROPERTY_BRIDGE_API_VERSION = "apiVersion";

    // List of all zone channel/property ids
    public static final String CHANNEL_ZONE_NAME = "name";
    public static final String CHANNEL_ZONE_ON_OFF = "onOff";
    public static final String CHANNEL_ZONE_TEMPERATURE = "temperature";
    public static final String CHANNEL_ZONE_HUMIDITY = "humidity";
    public static final String CHANNEL_ZONE_SETPOINT = "setpoint";
    public static final String CHANNEL_ZONE_HEAT_SETPOINT = "heatSetpoint";
    public static final String CHANNEL_ZONE_COOL_SETPOINT = "coolSetpoint";
    public static final String CHANNEL_ZONE_MODE = "mode";
    public static final String CHANNEL_ZONE_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_ZONE_HEAT_STAGE = "heatStage";
    public static final String CHANNEL_ZONE_COLD_STAGE = "coldStage";
    public static final String CHANNEL_ZONE_SLEEP = "sleep";
    public static final String CHANNEL_ZONE_ERRORS = "errors";
    public static final String CHANNEL_ZONE_AIR_DEMAND = "air-demand";
    public static final String CHANNEL_ZONE_FLOOR_DEMAND = "floor-demand";
    public static final String CHANNEL_ZONE_HEAT_DEMAND = "heat-demand";
    public static final String CHANNEL_ZONE_COLD_DEMAND = "cold-demand";
    public static final String CHANNEL_ZONE_AIR_QUALITY = "air-quality";
    public static final String CHANNEL_ZONE_AIR_QUALITY_MODE = "air-quality-mode";
    public static final String CHANNEL_ZONE_AIR_QUALITY_LOW_THRESHOLD = "air-quality-low-threshold";
    public static final String CHANNEL_ZONE_AIR_QUALITY_HIGH_THRESHOLD = "air-quality-high-threshold";
    public static final String CHANNEL_ZONE_SLATS_VERTICAL_SWING = "slats-vertical-swing";
    public static final String CHANNEL_ZONE_SLATS_HORIZONTAL_SWING = "slats-horizontal-swing";
    public static final String CHANNEL_ZONE_SLATS_VERTICAL_POSITION = "slats-vertical-position";
    public static final String CHANNEL_ZONE_SLATS_HORIZONTAL_POSITION = "slats-horizontal-position";

    public static final String PROPERTY_ZONE_THERMOS_TYPE = "thermosType";
    public static final String PROPERTY_ZONE_THERMOS_FIRMWARE = "thermosFirmware";
    public static final String PROPERTY_ZONE_THERMOS_RADIO = "thermosRadio";
    public static final String PROPERTY_ZONE_MASTER_ZONE_ID = "masterZoneId";
    public static final String PROPERTY_ZONE_AVAILABLE_MODES = "availableModes";
    public static final String PROPERTY_ZONE_AVAILABLE_SPEEDS = "availableSpeeds";
    public static final String PROPERTY_ZONE_AVAILABLE_COLD_STAGES = "availableColdStages";
    public static final String PROPERTY_ZONE_AVAILABLE_HEAT_STAGES = "availableHeatStages";

    // dynamic zone channel type ids
    public static final String CHANNEL_TYPE_ZONE_SPEED = "speed";
    public static final String CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE = "setpointTemperature";
    public static final String CHANNEL_TYPE_ZONE_DEMAND = "demand";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY_MODE = "air-quality-mode";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY = "air-quality";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY_THRESHOLD = "air-quality-threshold";
    public static final String CHANNEL_TYPE_ZONE_SLATS_SWING = "slats-swing";
    public static final String CHANNEL_TYPE_ZONE_SLATS_POSITION = "slats-position";

    // List of all system channel/property ids
    public static final String CHANNEL_SYSTEM_POWER = "power";

    // dynamic system channel type ids
    public static final String CHANNEL_TYPE_SYSTEM_POWER = "power";

    // Helper definitions
    public static final String BINDING_VALUES_SEPARATOR = ",";
    public static final String OUTPUT_VALUE_SEPARATOR = ",";
    public static final String UNKNOWN = "unknown";

    // Critical issues to be reported will use the following message
    public static final String LOGGING_CONTACT = "Please report to maintainer: ";

    public static final String UNKNOWN_THING_TYPE_ID = "FAILED";
    public static final String UNKNOWN_IP_ADDRESS = "xxx.xxx.xxx.xxx";

    // mode channel values
    public static final String ZONE_MODE_STOP = "STOP";
    public static final String ZONE_MODE_COOLING = "COOLING";
    public static final String ZONE_MODE_HEATING = "HEATING";
    public static final String ZONE_MODE_FAN = "FAN";
    public static final String ZONE_MODE_DRY = "DRY";
    public static final String ZONE_MODE_AUTO = "AUTO";

    // @formatter:off
    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<Integer, String> IntToZoneMode = Map.of(
        1, ZONE_MODE_STOP,
        2, ZONE_MODE_COOLING,
        3, ZONE_MODE_HEATING,
        4, ZONE_MODE_FAN,
        5, ZONE_MODE_DRY,
        7, ZONE_MODE_AUTO
    );

    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<String, Integer> ZoneModeToInt = Map.of(
        ZONE_MODE_STOP, 1,
        ZONE_MODE_COOLING, 2,
        ZONE_MODE_HEATING, 3,
        ZONE_MODE_FAN, 4,
        ZONE_MODE_DRY, 5,
        ZONE_MODE_AUTO, 7
    );
    // @formatter:on

    // stage channel values
    public static final String ZONE_STAGE_AIR = "AIR";
    public static final String ZONE_STAGE_RADIANT = "RADIANT";
    public static final String ZONE_STAGE_COMBINED = "COMBINED";

    // @formatter:off
    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<Integer, String> IntToStage = Map.of(
        1, ZONE_STAGE_AIR,
        2, ZONE_STAGE_RADIANT,
        3, ZONE_STAGE_COMBINED
    );

    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<String, Integer> ZoneStageToInt = Map.of(
        ZONE_STAGE_AIR, 1,
        ZONE_STAGE_RADIANT, 2,
        ZONE_STAGE_COMBINED, 3
    );
    // @formatter:on

    // sleep channel values
    public static final String ZONE_SLEEP_OFF = "OFF";
    public static final String ZONE_SLEEP_THIRTY = "THIRTY";
    public static final String ZONE_SLEEP_SIXTY = "SIXTY";
    public static final String ZONE_SLEEP_NINETY = "NINETY";

    // @formatter:off
    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<Integer, String> IntToSleep = Map.of(
        0, ZONE_SLEEP_OFF,
        30, ZONE_SLEEP_THIRTY,
        60, ZONE_SLEEP_SIXTY,
        90, ZONE_SLEEP_NINETY
    );

    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<String, Integer> ZoneSleepToInt = Map.of(
        ZONE_SLEEP_OFF, 0,
        ZONE_SLEEP_THIRTY, 30,
        ZONE_SLEEP_SIXTY, 60,
        ZONE_SLEEP_NINETY, 90
    );
    // @formatter:on

    // thermostat type property
    public static final String ZONE_THERMOSTAT_TYPE_BLUEFACE = "Blueface";
    public static final String ZONE_THERMOSTAT_TYPE_BLUEFACE_ZERO = "Blueface Zero";
    public static final String ZONE_THERMOSTAT_TYPE_LITE = "Lite";
    public static final String ZONE_THERMOSTAT_TYPE_THINK = "Think";

    // thermostat radio property
    public static final String ZONE_THERMOSTAT_RADIO_CABLE = "Cable";
    public static final String ZONE_THERMOSTAT_RADIO_RADIO = "Radio";

    // air quality mode channel values
    public static final String ZONE_AIR_QUALITY_MODE_OFF = "OFF";
    public static final String ZONE_AIR_QUALITY_MODE_ON = "ON";
    public static final String ZONE_AIR_QUALITY_MODE_AUTO = "AUTO";

    // @formatter:off
    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<Integer, String> IntToAirQualityMode = Map.of(
        0, ZONE_AIR_QUALITY_MODE_OFF,
        1, ZONE_AIR_QUALITY_MODE_ON,
        2, ZONE_AIR_QUALITY_MODE_AUTO
    );

    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<String, Integer> ZoneAirQualityModeToInt = Map.of(
        ZONE_AIR_QUALITY_MODE_OFF, 0,
        ZONE_AIR_QUALITY_MODE_ON, 1,
        ZONE_AIR_QUALITY_MODE_AUTO, 2
    );
    // @formatter:on

    // air quality channel values
    public static final String ZONE_AIR_QUALITY_OFF = "OFF";
    public static final String ZONE_AIR_QUALITY_GOOD = "GOOD";
    public static final String ZONE_AIR_QUALITY_MEDIUM = "MEDIUM";
    public static final String ZONE_AIR_QUALITY_LOW = "LOW";

    // @formatter:off
    @SuppressWarnings("null") // the "of" method has no annotations despite it not returning Null
    public static final Map<Integer, String> IntToAirQuality = Map.of(
        0, ZONE_AIR_QUALITY_MODE_OFF,
        1, ZONE_AIR_QUALITY_GOOD,
        2, ZONE_AIR_QUALITY_MEDIUM,
        3, ZONE_AIR_QUALITY_LOW
    );
    // @formatter:on
}
