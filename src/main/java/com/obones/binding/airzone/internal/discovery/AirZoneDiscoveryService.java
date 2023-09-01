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
package com.obones.binding.airzone.internal.discovery;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacResponse;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacSystemsResponse;
import com.obones.binding.airzone.internal.api.model.AirZoneHvacZone;
import com.obones.binding.airzone.internal.handler.AirZoneBridgeHandler;
import com.obones.binding.airzone.internal.utils.Localization;
import com.obones.binding.airzone.internal.utils.ManifestInformation;

/**
 * The {@link AirZoneDiscoveryService} is responsible for discovering zones on the current AirZone Bridge.
 *
 * @author Olivier Sannier - Initial contribution.
 */
@NonNullByDefault
public class AirZoneDiscoveryService extends AbstractDiscoveryService implements Runnable {

    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneDiscoveryService.class);

    // Class internal

    private static final int DISCOVER_TIMEOUT_SECONDS = 10;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;

    // Private

    @SuppressWarnings("null") // unexplained warnings on Localization constructor
    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link AirZoneDiscoveryService} without any further information.
     */
    public AirZoneDiscoveryService() {
        super(AirZoneBindingConstants.DISCOVERABLE_THINGS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("AirZoneDiscoveryService(without Bridge) just initialized.");
    }

    @Reference
    protected void setLocaleProvider(final LocaleProvider givenLocaleProvider) {
        logger.trace("setLocaleProvider(): provided locale={}.", givenLocaleProvider);
        localeProvider = givenLocaleProvider;
        updateLocalization();
    }

    @Reference
    protected void setTranslationProvider(TranslationProvider givenI18nProvider) {
        logger.trace("setTranslationProvider(): provided translation={}.", givenI18nProvider);
        i18nProvider = givenI18nProvider;
        updateLocalization();
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link AirZoneDiscoveryService} with a reference to the well-prepared environment with a
     * {@link AirZoneBridgeHandler}.
     *
     * @param localizationHandler Initialized localization handler.
     */
    public AirZoneDiscoveryService(Localization localizationHandler) {
        super(AirZoneBindingConstants.DISCOVERABLE_THINGS, DISCOVER_TIMEOUT_SECONDS);
        logger.trace("AirZoneDiscoveryService(locale={},i18n={}) just initialized.", localeProvider, i18nProvider);
        localization = localizationHandler;
    }

    /**
     * Constructor
     * <P>
     * Initializes the {@link AirZoneDiscoveryService} with a reference to the well-prepared environment with a
     * {@link AirZoneBridgeHandler}.
     *
     * @param locationProvider Provider for a location.
     * @param localeProvider Provider for a locale.
     * @param i18nProvider Provider for the internationalization.
     */
    public AirZoneDiscoveryService(LocationProvider locationProvider, LocaleProvider localeProvider,
            TranslationProvider i18nProvider) {
        this(new Localization(localeProvider, i18nProvider));
        logger.trace("AirZoneDiscoveryService(locale={},i18n={}) finished.", localeProvider, i18nProvider);
    }

    @Override
    public void deactivate() {
        logger.warn("deactivate() called.");
        super.deactivate();
    }

    @Override
    protected synchronized void startScan() {
        logger.trace("startScan() called.");

        logger.debug("startScan(): creating a thing of type binding.");
        ThingUID thingUID = new ThingUID(AirZoneBindingConstants.THING_TYPE_BINDING, "com_obones_binding_airzone");
        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                .withProperty(AirZoneBindingProperties.PROPERTY_BINDING_BUNDLEVERSION,
                        ManifestInformation.getBundleVersion())
                .withRepresentationProperty(AirZoneBindingProperties.PROPERTY_BINDING_BUNDLEVERSION)
                .withLabel(localization.getText("discovery.airzone.binding...label")).build();
        logger.debug("startScan(): registering new thing {}.", discoveryResult);
        thingDiscovered(discoveryResult);

        // Here would be the place to discover bridges, maybe via posting a method to scheduler.execute(()

        logger.trace("startScan() done.");
    }

    @Override
    public synchronized void stopScan() {
        logger.trace("stopScan() called.");
        super.stopScan();
        logger.trace("stopScan() done.");
    }

    @Override
    public void run() {
        logger.trace("run() called.");
    }

    /**
     * Discover the registered zones.
     */
    public void discoverZones(AirZoneApiManager apiManager, ThingUID bridgeUID) {
        logger.trace("discoverZones(): discovering all zones on bridge {}.", bridgeUID);

        @Nullable
        AirZoneHvacResponse latestResponse = apiManager.getLatestZonesResponse();
        if (latestResponse != null) {
            for (var system : latestResponse.getSystems()) {
                for (var zone : system.getData()) {
                    String zoneName = zone.getName().toString();
                    logger.trace("discoverZones(): found zone {}.", zoneName);

                    String label = "AirZone - ".concat(zoneName.replaceAll("\\P{Alnum}", "_"));
                    logger.trace("discoverZones(): using label {}.", label);

                    String zoneUniqueId = AirZoneBridgeHandler.getZoneUniqueId(zone.getSystemID(), zone.getZoneID());

                    ThingTypeUID thingTypeUID = AirZoneBindingConstants.THING_TYPE_AIRZONE_ZONE;
                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, zoneUniqueId);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withThingType(thingTypeUID)
                            .withProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_ID, zone.getSystemID())
                            .withProperty(AirZoneBindingProperties.PROPERTY_ZONE_ID, zone.getZoneID())
                            .withProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID, zoneUniqueId)
                            .withRepresentationProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID)
                            .withBridge(bridgeUID).withLabel(label).build();
                    logger.debug("discoverZones(): registering new thing {}.", discoveryResult);
                    thingDiscovered(discoveryResult);
                }

                // add the "all zones" thing
                @Nullable
                AirZoneHvacZone masterZone = apiManager.getMasterZone(system);
                if (masterZone != null) {
                    int systemId = masterZone.getSystemID();
                    String allZonesUniqueId = AirZoneBridgeHandler.getZoneUniqueId(systemId, 0);

                    String label = String.format("AirZone - All zones (system %d)", systemId);

                    ThingTypeUID allZonesThingTypeUID = AirZoneBindingConstants.THING_TYPE_AIRZONE_ALL_ZONES;
                    ThingUID allZonesThingUID = new ThingUID(allZonesThingTypeUID, bridgeUID, allZonesUniqueId);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(allZonesThingUID)
                            .withThingType(allZonesThingTypeUID)
                            .withProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_ID, systemId)
                            .withProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID, allZonesUniqueId)
                            .withRepresentationProperty(AirZoneBindingProperties.PROPERTY_ZONE_UNIQUE_ID)
                            .withBridge(bridgeUID).withLabel(label).build();
                    logger.debug("discoverZones(): registering new \"all zones\" thing {}.", discoveryResult);
                    thingDiscovered(discoveryResult);
                }
            }
        }
        logger.trace("discoverZones() finished.");
    }

    /**
     * Discover the registered zones.
     */
    public void discoverSystems(@Nullable AirZoneHvacSystemsResponse latestResponse, ThingUID bridgeUID) {
        logger.trace("discoverSystems(): discovering all systems on bridge {}.", bridgeUID);

        if (latestResponse != null) {
            var systems = latestResponse.getSystems();
            if (systems != null) {
                for (var system : systems) {
                    String systemName = system.getManufacturer().toString();
                    logger.trace("discoverSystems(): found system {}.", systemName);

                    String label = "AirZone - System - ".concat(systemName.replaceAll("\\P{Alnum}", "_"));
                    logger.trace("discoverSystems(): using label {}.", label);

                    String systemUniqueId = AirZoneBridgeHandler.getSystemUniqueId(system.getSystemID());

                    ThingTypeUID thingTypeUID = AirZoneBindingConstants.THING_TYPE_AIRZONE_SYSTEM;
                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, systemUniqueId);

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                            .withThingType(thingTypeUID)
                            .withProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_ID, system.getSystemID())
                            .withProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_UNIQUE_ID, systemUniqueId)
                            .withRepresentationProperty(AirZoneBindingProperties.PROPERTY_SYSTEM_UNIQUE_ID)
                            .withBridge(bridgeUID).withLabel(label).build();
                    logger.debug("discoverSystems(): registering new thing {}.", discoveryResult);
                    thingDiscovered(discoveryResult);
                }
            }
        }
    }
}
