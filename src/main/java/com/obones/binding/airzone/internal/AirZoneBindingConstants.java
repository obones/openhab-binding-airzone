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
 * <LI>{@link #THING_AIRZONE_ALL_ZONES} for the special "all zones" item that controls all zones known by AirZone</LI>
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
    /**
     * The Thing identification of the special "all zones" to control all zones in the <B>AirZone</B> bridge.
     */
    private static final String THING_AIRZONE_ALL_ZONES = "all-zones";
    /**
     * The Thing identification of a system defined on the <B>AirZone</B> bridge.
     */
    private static final String THING_AIRZONE_SYSTEM = "system";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BINDING = new ThingTypeUID(BINDING_ID, THING_AIRZONE_BINDING);

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_AIRZONE_BRIDGE);

    // List of all Thing Type UIDs beyond the bridge(s)
    public static final ThingTypeUID THING_TYPE_AIRZONE_ZONE = new ThingTypeUID(BINDING_ID, THING_AIRZONE_ZONE);
    public static final ThingTypeUID THING_TYPE_AIRZONE_ALL_ZONES = new ThingTypeUID(BINDING_ID,
            THING_AIRZONE_ALL_ZONES);
    public static final ThingTypeUID THING_TYPE_AIRZONE_SYSTEM = new ThingTypeUID(BINDING_ID, THING_AIRZONE_SYSTEM);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BINDING = new HashSet<>(Arrays.asList(THING_TYPE_BINDING));
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));

    public static final Set<ThingTypeUID> SUPPORTED_THINGS_ITEMS = new HashSet<>(
            Arrays.asList(THING_TYPE_AIRZONE_ZONE, THING_TYPE_AIRZONE_ALL_ZONES, THING_TYPE_AIRZONE_SYSTEM));

    public static final Set<ThingTypeUID> DISCOVERABLE_THINGS = Set.of(THING_TYPE_AIRZONE_ZONE, THING_TYPE_BINDING,
            THING_TYPE_BRIDGE);

    // *** List of all Channel ids ***

    // List of all binding channel ids

    /** Channel identifier describing the current Binding State. */
    public static final String CHANNEL_BINDING_INFORMATION = "information";

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
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
    public static final String CHANNEL_ZONE_ON_OFF = "on-off";
    public static final String CHANNEL_ZONE_TEMPERATURE = "temperature";
    public static final String CHANNEL_ZONE_HUMIDITY = "humidity";
    public static final String CHANNEL_ZONE_SETPOINT = "setpoint";
    public static final String CHANNEL_ZONE_HEAT_SETPOINT = "heat-setpoint";
    public static final String CHANNEL_ZONE_COOL_SETPOINT = "cool-setpoint";
    public static final String CHANNEL_ZONE_MODE = "mode";
    public static final String CHANNEL_ZONE_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_ZONE_HEAT_STAGE = "heat-stage";
    public static final String CHANNEL_ZONE_COLD_STAGE = "cold-stage";
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
    public static final String CHANNEL_ZONE_ECO_ADAPT = "eco-adapt";
    public static final String CHANNEL_ZONE_ANTI_FREEZE = "anti-freeze";

    public static final String PROPERTY_ZONE_THERMOS_TYPE = "thermosType";
    public static final String PROPERTY_ZONE_THERMOS_FIRMWARE = "thermosFirmware";
    public static final String PROPERTY_ZONE_THERMOS_RADIO = "thermosRadio";
    public static final String PROPERTY_ZONE_MASTER_ZONE_ID = "masterZoneId";
    public static final String PROPERTY_ZONE_AVAILABLE_MODES = "availableModes";
    public static final String PROPERTY_ZONE_AVAILABLE_SPEEDS = "availableSpeeds";
    public static final String PROPERTY_ZONE_AVAILABLE_COLD_STAGES = "availableColdStages";
    public static final String PROPERTY_ZONE_AVAILABLE_HEAT_STAGES = "availableHeatStages";

    // dynamic zone channel type ids
    public static final String CHANNEL_TYPE_ZONE_ON_OFF = "on-off";
    public static final String CHANNEL_TYPE_ZONE_MODE = "mode";
    public static final String CHANNEL_TYPE_ZONE_SPEED = "speed";
    public static final String CHANNEL_TYPE_ZONE_SLEEP = "sleep";
    public static final String CHANNEL_TYPE_ZONE_SETPOINT_TEMPERATURE = "setpoint-temperature";
    public static final String CHANNEL_TYPE_ZONE_STAGE = "stage";
    public static final String CHANNEL_TYPE_ZONE_DEMAND = "demand";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY_MODE = "air-quality-mode";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY = "air-quality";
    public static final String CHANNEL_TYPE_ZONE_AIR_QUALITY_THRESHOLD = "air-quality-threshold";
    public static final String CHANNEL_TYPE_ZONE_SLATS_SWING = "slats-swing";
    public static final String CHANNEL_TYPE_ZONE_SLATS_POSITION = "slats-position";
    public static final String CHANNEL_TYPE_ZONE_ECO_ADAPT = "eco-adapt";
    public static final String CHANNEL_TYPE_ZONE_ANTI_FREEZE = "anti-freeze";

    // List of all system channel/property ids
    public static final String CHANNEL_SYSTEM_POWER = "power";
    public static final String CHANNEL_SYSTEM_ERRORS = "errors";

    public static final String PROPERTY_SYSTEM_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_SYSTEM_SYSTEM_TYPE = "systemType";
    public static final String PROPERTY_SYSTEM_SYSTEM_FIRMWARE = "systemFirmware";
    public static final String PROPERTY_SYSTEM_METER_CONNECTED = "meterConnected";

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
    public static final Map<Integer, String> IntToZoneMode = Map.of(
        1, ZONE_MODE_STOP,
        2, ZONE_MODE_COOLING,
        3, ZONE_MODE_HEATING,
        4, ZONE_MODE_FAN,
        5, ZONE_MODE_DRY,
        7, ZONE_MODE_AUTO
    );

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
    public static final Map<Integer, String> IntToStage = Map.of(
        1, ZONE_STAGE_AIR,
        2, ZONE_STAGE_RADIANT,
        3, ZONE_STAGE_COMBINED
    );

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
    public static final Map<Integer, String> IntToSleep = Map.of(
        0, ZONE_SLEEP_OFF,
        30, ZONE_SLEEP_THIRTY,
        60, ZONE_SLEEP_SIXTY,
        90, ZONE_SLEEP_NINETY
    );

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
    public static final Map<Integer, String> IntToAirQualityMode = Map.of(
        0, ZONE_AIR_QUALITY_MODE_OFF,
        1, ZONE_AIR_QUALITY_MODE_ON,
        2, ZONE_AIR_QUALITY_MODE_AUTO
    );

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
    public static final Map<Integer, String> IntToAirQuality = Map.of(
        0, ZONE_AIR_QUALITY_OFF,
        1, ZONE_AIR_QUALITY_GOOD,
        2, ZONE_AIR_QUALITY_MEDIUM,
        3, ZONE_AIR_QUALITY_LOW
    );
    // @formatter:on

    // eco adapt channel values
    public static final String ZONE_ECO_ADAPT_OFF = "OFF";
    public static final String ZONE_ECO_ADAPT_MANUAL = "MANUAL";
    public static final String ZONE_ECO_ADAPT_A = "A";
    public static final String ZONE_ECO_ADAPT_A_PLUS = "A_PLUS";
    public static final String ZONE_ECO_ADAPT_A_PLUS_PLUS = "A_PLUS_PLUS";

    // @formatter:off
    public static final Map<String, String> StringToEcoAdapt = Map.of(
        "off", ZONE_ECO_ADAPT_OFF,
        "manual", ZONE_ECO_ADAPT_MANUAL,
        "a", ZONE_ECO_ADAPT_A,
        "a_p", ZONE_ECO_ADAPT_A_PLUS,
        "a_pp", ZONE_ECO_ADAPT_A_PLUS_PLUS
    );

    public static final Map<String, String> EcoAdaptToString = Map.of(
        ZONE_ECO_ADAPT_OFF, "off",
        ZONE_ECO_ADAPT_MANUAL, "manual",
        ZONE_ECO_ADAPT_A, "a",
        ZONE_ECO_ADAPT_A_PLUS, "a_p",
        ZONE_ECO_ADAPT_A_PLUS_PLUS, "a_pp"
    );
    // @formatter:on

    // system type property
    public static final String SYSTEM_SYSTEM_TYPE_C6 = "C6";
    public static final String SYSTEM_SYSTEM_TYPE_AQUAGLASS = "AQUAGLASS";
    public static final String SYSTEM_SYSTEM_TYPE_DZK = "DZK";
    public static final String SYSTEM_SYSTEM_TYPE_RADIANT = "Radiant";
    public static final String SYSTEM_SYSTEM_TYPE_C3 = "C3";
    public static final String SYSTEM_SYSTEM_TYPE_ZBS = "ZBS";
    public static final String SYSTEM_SYSTEM_TYPE_ZS6 = "ZS6";
}
