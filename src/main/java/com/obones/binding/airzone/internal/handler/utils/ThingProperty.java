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
package com.obones.binding.airzone.internal.handler.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * The class {@link ThingProperty} provides methods for dealing with
 * properties.
 * <ul>
 * <li>{@link ThingConfiguration#exists} Check existence of a property,</LI>
 * <li>{@link ThingConfiguration#getValue} Returns a property value,</LI>
 * <li>{@link #setValue} Modifies a property value.</LI>
 * </UL>
 * <P>
 * Noninstantiable utility class
 * </P>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class ThingProperty {
    private @NonNullByDefault({}) static final Logger LOGGER = LoggerFactory.getLogger(ThingProperty.class);

    /*
     * ************************
     * ***** Constructors *****
     */

    // Suppress default constructor for non-Instantiability

    private ThingProperty() {
        throw new AssertionError();
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Modifies the property value of the given thing and the named property.
     * <p>
     *
     * @param thing which property will be modified,
     * @param propertyName defines the property which is to be modified,
     * @param propertyValue defines the new property value.
     */
    public static void setValue(Thing thing, String propertyName, @Nullable String propertyValue) {
        thing.setProperty(propertyName, propertyValue);
        LOGGER.trace("setValue() {} set to {}.", propertyName, propertyValue);
        return;
    }

    /**
     * Modifies the property value for the given bridge, which is a dedicated thing, and the named property.
     * <p>
     *
     * @param bridgeHandler which contains the properties,
     * @param propertyName defines the property which is to be modified.
     * @param propertyValue defines the new property value.
     */
    public static void setValue(BaseBridgeHandler bridgeHandler, String propertyName,
            @Nullable String propertyValue) {
        setValue(bridgeHandler.getThing(), propertyName, propertyValue);
    }

    /**
     * Modifies the property value for the given propertyName, identified by the given bridge and channel.desired
     * propertyName which are defined within
     * AirZoneBindingProperties.
     * <p>
     *
     * @param bridgeHandler which contains the properties,
     * @param channelUID describes the channel to by scrutinized,
     * @param propertyName defines the property which is to be modified.
     * @param propertyValue defines the new property value.
     */
    public static void setValue(BaseBridgeHandler bridgeHandler, ChannelUID channelUID, String propertyName,
            @Nullable String propertyValue) {
        ThingUID channelTUID = channelUID.getThingUID();
        Thing thingOfChannel = bridgeHandler.getThing().getThing(channelTUID);
        if (thingOfChannel == null) {
            LOGGER.warn("setValue(): Channel {} does not belong to a thing.", channelUID);
            return;
        }
        setValue(thingOfChannel, propertyName, propertyValue);
    }
}
