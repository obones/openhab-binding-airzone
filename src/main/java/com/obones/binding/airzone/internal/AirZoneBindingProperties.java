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
package com.obones.binding.airzone.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AirZoneBindingProperties} class defines common constants, which are
 * used within the property definitions.
 *
 * This class contains the property identifications:
 * <UL>
 * <LI>{@link #PROPERTY_BINDING_BUNDLEVERSION} for identification of the binding,</LI>
 * <LI>{@link #PROPERTY_BINDING_NOOFBRIDGES} for number of bridges,</LI>
 * <LI>{@link #PROPERTY_BINDING_NOOFTHINGS} for number of things,</LI>
 * </UL>
 * <UL>
 * <LI>{@link #PROPERTY_SYSTEM_ID} for defining the id of a system</LI>
 * <LI>{@link #PROPERTY_ZONE_ID} for defining the id of a zone</LI>
 * <LI>{@link #PROPERTY_ZONE_UNIQUE_ID} for defining the unique id of a zone</LI>
 * </UL>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneBindingProperties {
    public static final String PROPERTY_BINDING_BUNDLEVERSION = "bundleVersion";
    public static final String PROPERTY_BINDING_NOOFBRIDGES = "numberOfBridges";
    public static final String PROPERTY_BINDING_NOOFTHINGS = "numberOfThings";

    public static final String PROPERTY_SYSTEM_ID = "systemId";
    public static final String PROPERTY_ZONE_ID = "zoneId";
    public static final String PROPERTY_ZONE_UNIQUE_ID = "zoneUniqueId";
    public static final String PROPERTY_SYSTEM_UNIQUE_ID = "systemUniqueId";
}
