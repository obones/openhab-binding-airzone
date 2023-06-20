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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AirZoneBindingConstants} class defines common constants, which are
 * used across the whole binding.
 * <P>
 * For an in-depth view of the available Item types with description of parameters, take a look onto
 * {@link com.obones.binding.airzone.internal.AirZoneItemType AirZoneItemType}.
 * </P>
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

    // Helper definitions
    public static final String BINDING_VALUES_SEPARATOR = ",";
    public static final String OUTPUT_VALUE_SEPARATOR = ",";
    public static final String UNKNOWN = "unknown";

    // Critical issues to be reported will use the following message
    public static final String LOGGING_CONTACT = "Please report to maintainer: ";

    public static final String UNKNOWN_THING_TYPE_ID = "FAILED";
    public static final String UNKNOWN_IP_ADDRESS = "xxx.xxx.xxx.xxx";
}
