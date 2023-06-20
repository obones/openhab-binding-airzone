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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Enumeration of Types of a AirZone item.
 * <br>
 * Provides information about:
 * <ul>
 * <li>associated thing identified by String</li>
 * <li>defined channel identified by String</li>
 * <li>{@link #getItemClass} item class,</li>
 * <li>{@link #isReadable} about a read possibility,</li>
 * <li>{@link #isWritable} about a write possibility,</li>
 * <li>{@link #isExecutable} about an execute possibility,</li>
 * <li>{@link #isToBeRefreshed} about necessarily to be refreshed,</li>
 * <li>{@link #isToBeRefreshedNow} about necessarily to be refreshed at this time,</li>
 * <li>{@link #isChannel} as indication of being handled as Channel of a thing,</li>
 * <li>{@link #isProperty} as indication of being handled as property of a thing.</li>
 * </ul>
 *
 * In addition there are helper methods providing information about:
 *
 * <ul>
 * <li>{@link #getIdentifier} returning the common identifier string,</li>
 * <li>{@link #getByThingAndChannel} to retrieve an enum instance selected by Thing
 * and Channel identifier,</li>
 * <li>{@link #getPropertyEntriesByThing} to retrieve any Thing identifiers as array of
 * String,</li>
 * </ul>
 * <p>
 * Within this enumeration, the expected behavior of the OpenHAB item (resp. Channel or Property) is set. For each kind
 * of Channel (i.e. bridge or device) parameter a set of information is defined:
 * <ul>
 * <li>
 * Unique identification by:
 * <ul>
 * <li>Thing name as string,</li>
 * <li>Channel name as string,</li>
 * </ul>
 * </li>
 * <li>Channel type as OpenHAB type,</li>
 * <li>ability flag whether this item is to be read,</li>
 * <li>ability flag whether this item is able to be modified,</li>
 * <li>ability flag whether this item is to be used as execution trigger.</li>
 * </ul>
 *
 * @author Olivier Sannier - Initial contribution
 *
 */
@NonNullByDefault
public enum AirZoneItemType {
    // @formatter:off
    UNKNOWN(AirZoneBindingConstants.THING_TYPE_BRIDGE,                            AirZoneBindingConstants.UNKNOWN,                       TypeFlavor.UNUSABLE),
    //
    BINDING_INFORMATION(AirZoneBindingConstants.THING_TYPE_BINDING,               AirZoneBindingConstants.CHANNEL_BINDING_INFORMATION,   TypeFlavor.READONLY_VOLATILE_STRING),
    //
    BRIDGE_STATUS(AirZoneBindingConstants.THING_TYPE_BRIDGE,                      AirZoneBindingConstants.CHANNEL_BRIDGE_STATUS,         TypeFlavor.READONLY_VOLATILE_STRING),
    BRIDGE_DOWNTIME(AirZoneBindingConstants.THING_TYPE_BRIDGE,                    AirZoneBindingConstants.CHANNEL_BRIDGE_DOWNTIME,       TypeFlavor.READONLY_VOLATILE_NUMBER),
    BRIDGE_RELOAD(AirZoneBindingConstants.THING_TYPE_BRIDGE,                      AirZoneBindingConstants.CHANNEL_BRIDGE_RELOAD,         TypeFlavor.INITIATOR),
    BRIDGE_DO_DETECTION(AirZoneBindingConstants.THING_TYPE_BRIDGE,                AirZoneBindingConstants.CHANNEL_BRIDGE_DO_DETECTION,   TypeFlavor.INITIATOR),
    ;
    // @formatter:on

    private enum TypeFlavor {
        /**
         * Used to present read-only non-volatile configuration parameters as StringItem.
         */
        READONLY_STATIC_STRING,
        /**
         * Used to present read-only non-volatile configuration parameters as SwitchItem.
         */
        READONLY_STATIC_SWITCH,
        /**
         * Used to present volatile configuration parameters as StringItem.
         */
        READONLY_VOLATILE_STRING,
        /**
         * Used to present volatile configuration parameters as NumberItem.
         */
        READONLY_VOLATILE_NUMBER,
        /**
         * Used to present volatile configuration parameters as NumberItem.
         */
        WRITEONLY_VOLATILE_SWITCH,
        /**
         * Used to present volatile configuration parameters as SwitchItem.
         */
        READWRITE_VOLATILE_SWITCH,
        /**
         * Used to initiate an action.
         */
        INITIATOR,
        /**
         * Used to manipulate an actuator.
         */
        MANIPULATOR_SWITCH,
        /**
         * Used to present read-only non-volatile configuration parameter (being handled as property of aThing).
         */
        PROPERTY,
        /**
         * Used to define an UNUSABLE entry.
         */
        UNUSABLE
    }

    /*
     * ***************************
     * ***** Private Objects *****
     */

    private ThingTypeUID thingIdentifier;
    private String channelIdentifier;
    private Class<? extends GenericItem> itemClass;
    private boolean itemIsReadable;
    private boolean itemIsWritable;
    private boolean itemIsExecutable;
    private boolean itemIsToBeRefreshed;
    private int itemsRefreshDivider;
    private boolean itemIsChannel;
    private boolean itemIsProperty;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirZoneItemType.class);

    private static final int REFRESH_CYCLE_FIRST_TIME = 0;
    private static final int REFRESH_ONCE_A_DAY = 8640;
    private static final int REFRESH_EACH_HOUR = 360;
    private static final int REFRESH_EACH_MINUTE = 6;
    private static final int REFRESH_EVERY_CYCLE = 1;

    /*
     * ************************
     * ***** Constructors *****
     */

    AirZoneItemType(ThingTypeUID thingIdentifier, String channelIdentifier, TypeFlavor typeFlavor) {
        this.thingIdentifier = thingIdentifier;
        this.channelIdentifier = channelIdentifier;
        this.itemIsChannel = true;
        this.itemIsProperty = false;
        switch (typeFlavor) {
            case READONLY_STATIC_STRING:
                this.itemClass = StringItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
                break;
            case READONLY_STATIC_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
                break;

            case READONLY_VOLATILE_STRING:
                this.itemClass = StringItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;
            case READONLY_VOLATILE_NUMBER:
                this.itemClass = NumberItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EVERY_CYCLE;
                break;
            case WRITEONLY_VOLATILE_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;
            case READWRITE_VOLATILE_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EVERY_CYCLE;
                break;

            case INITIATOR:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = false;
                this.itemIsExecutable = true;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = 1;
                break;

            case MANIPULATOR_SWITCH:
                this.itemClass = SwitchItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = true;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_MINUTE;
                break;

            case PROPERTY:
                this.itemClass = StringItem.class;
                this.itemIsReadable = true;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = true;
                this.itemsRefreshDivider = REFRESH_EACH_HOUR;
                this.itemIsChannel = false;
                this.itemIsProperty = true;
                break;

            case UNUSABLE:
            default:
                this.itemClass = StringItem.class;
                this.itemIsReadable = false;
                this.itemIsWritable = false;
                this.itemIsExecutable = false;
                this.itemIsToBeRefreshed = false;
                this.itemsRefreshDivider = REFRESH_ONCE_A_DAY;
                this.itemIsChannel = false;
        }
    }

    /*
     * ********************************
     * ***** Class access methods *****
     */

    @Override
    public String toString() {
        return this.thingIdentifier + "/" + this.channelIdentifier;
    }

    /**
     * {@link AirZoneItemType} access method to query Identifier on this type of item.
     *
     * @return <b>thingIdentifier</b> of type String describing the value of the enum {@link AirZoneItemType}
     *         return
     */
    public ThingTypeUID getThingTypeUID() {
        return this.thingIdentifier;
    }

    /**
     * {@link AirZoneItemType} access method to query common (channel/property) identifier on this type of item.
     *
     * @return <b>channelIdentifier</b> of type String describing the value of the enum {@link AirZoneItemType}.
     */
    public String getIdentifier() {
        return this.channelIdentifier;
    }

    /**
     * {@link AirZoneItemType} access method to query the appropriate type of item.
     *
     * @return <b>itemClass</b> of type Item describing the possible type of this item.
     */
    public Class<? extends GenericItem> getItemClass() {
        return this.itemClass;
    }

    /**
     * {@link AirZoneItemType} access method to query Read possibility on this type of item.
     *
     * @return <b>itemIsReadable</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isReadable() {
        return this.itemIsReadable;
    }

    /**
     * {@link AirZoneItemType} access method to query Write possibility on this type of item.
     *
     * @return <b>itemIsWritable</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isWritable() {
        return this.itemIsWritable;
    }

    /**
     * {@link AirZoneItemType} access method to query Execute possibility on this type of item.
     *
     * @return <b>isExecute</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isExecutable() {
        return this.itemIsExecutable;
    }

    /**
     * {@link AirZoneItemType} access method to query the need of refresh on this type of item.
     *
     * @return <b>isExecute</b> of type boolean describing the ability to perform a write operation.
     */
    public boolean isToBeRefreshed() {
        return this.itemIsToBeRefreshed;
    }

    /**
     * {@link AirZoneItemType} access method to query the refreshMSecs interval on this type of item.
     *
     * @return <b>refreshDivider</b> of type int describing the factor.
     */
    public int getRefreshDivider() {
        return this.itemsRefreshDivider;
    }

    /**
     * {@link AirZoneItemType} access method to query the type of this item.
     *
     * @return <b>isChannel</b> of type boolean describing the need to be handled as channel.
     */
    public boolean isChannel() {
        return this.itemIsChannel;
    }

    /**
     * {@link AirZoneItemType} access method to query the type of this item.
     *
     * @return <b>itemIsProperty</b> of type boolean describing the need to be handled as property.
     */
    public boolean isProperty() {
        return this.itemIsProperty;
    }

    /**
     * {@link AirZoneItemType} access method to find an enum by itemTypeName.
     *
     * @param itemTypeName as name of requested Thing of type String.
     *
     * @return <b>airZoneItemType</b> of type AirZoneItemType describing the appropriate enum.
     */
    public AirZoneItemType getByString(String itemTypeName) {
        try {
            return AirZoneItemType.valueOf(itemTypeName);
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }

    /**
     * {@link AirZoneItemType} access method to find an enum by name.
     *
     * @param thingIdentifier as name of requested Thing of type String.
     * @param channelIdentifier as name of requested Channel of type String.
     *
     * @return <b>airZoneItemType</b> of type AirZoneItemType describing the appropriate enum.
     */
    public static AirZoneItemType getByThingAndChannel(ThingTypeUID thingIdentifier, String channelIdentifier) {
        for (AirZoneItemType v : AirZoneItemType.values()) {
            if (thingIdentifier.equals(v.thingIdentifier) && channelIdentifier.equals(v.channelIdentifier)) {
                LOGGER.trace("getByThingAndChannel({},{}) returns enum {}.", thingIdentifier, channelIdentifier, v);
                return v;
            }
        }
        LOGGER.trace("getByThingAndChannel({},{}) returns enum UNKNOWN.", thingIdentifier, channelIdentifier);
        return UNKNOWN;
    }

    /**
     * {@link AirZoneItemType} access method to find similar enum entries by thingIdentifier.
     *
     * @param thingIdentifier as name of requested Thing of type String.
     *
     * @return <b>listOfairZoneItemType</b> of type List of AirZoneItemType containing all similar enum entries.
     */
    public static List<AirZoneItemType> getPropertyEntriesByThing(ThingTypeUID thingIdentifier) {
        List<AirZoneItemType> list = new ArrayList<>();
        for (AirZoneItemType v : AirZoneItemType.values()) {
            if (thingIdentifier.equals(v.thingIdentifier) && v.itemIsProperty) {
                list.add(v);
            }
        }
        LOGGER.trace("getPropertyEntriesByThing({}) returns {}.", thingIdentifier, list);
        return list;
    }

    /**
     * Helper function: Calculate modulo.
     *
     * @param a as dividend.
     * @param b as divisor.
     *
     * @return <b>true</b> if zero is remainder after division.
     */
    private static boolean isModulo(int a, int b) {
        return (a % b) == 0;
    }

    /**
     * {@link AirZoneItemType} access method to determine the necessity of being refreshed
     * within the current refresh cycle.
     *
     * @param refreshCycleCounter as identification of the refresh round.
     * @param thingIdentifier as name of requested Thing.
     * @param channelIdentifier as name of requested Channel.
     *
     * @return <b>boolean</b> value which expresses the need.
     */
    public static boolean isToBeRefreshedNow(int refreshCycleCounter, ThingTypeUID thingIdentifier,
            String channelIdentifier) {
        AirZoneItemType itemType = getByThingAndChannel(thingIdentifier, channelIdentifier);

        if (itemType == AirZoneItemType.UNKNOWN) {
            LOGGER.warn("isToBeRefreshedNow({},{},{}): returning false, as item is not found.", refreshCycleCounter,
                    thingIdentifier, channelIdentifier);
            return false;
        }

        if (((refreshCycleCounter == REFRESH_CYCLE_FIRST_TIME) && (itemType.isReadable()))
                || (itemType.isToBeRefreshed())) {
            if ((refreshCycleCounter == REFRESH_CYCLE_FIRST_TIME)
                    || (isModulo(refreshCycleCounter, itemType.getRefreshDivider()))) {
                LOGGER.trace("isToBeRefreshedNow(): returning true, as item is to be refreshed, now.");
                return true;
            } else {
                LOGGER.trace("isToBeRefreshedNow(): returning false, as refresh cycle has not yet come for this item.");
            }
        } else {
            LOGGER.trace("isToBeRefreshedNow(): returning false, as item is not refreshable.");
        }
        return false;
    }
}
