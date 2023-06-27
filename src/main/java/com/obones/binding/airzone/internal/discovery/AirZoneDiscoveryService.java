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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.LocationProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.AirZoneBindingProperties;
import com.obones.binding.airzone.internal.api.AirZoneApiManager;
import com.obones.binding.airzone.internal.handler.AirZoneBridgeHandler;
import com.obones.binding.airzone.internal.utils.Localization;
import com.obones.binding.airzone.internal.utils.ManifestInformation;

/**
 * The {@link AirZoneDiscoveryService} is responsible for discovering zones on the current AirZone Bridge.
 *
 * @author Olivier Sannier - Initial contribution.
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery.airzone")
public class AirZoneDiscoveryService extends AbstractDiscoveryService implements Runnable {
    
    private final Logger logger = LoggerFactory.getLogger(AirZoneDiscoveryService.class);

    // Class internal

    private static final int DISCOVER_TIMEOUT_SECONDS = 60;

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;
    private final Set<AirZoneBridgeHandler> bridgeHandlers = new HashSet<>();

    @Nullable
    private ScheduledFuture<?> backgroundTask = null;

    // Private

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
        logger.trace("deactivate() called.");
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

        /*scheduler.execute(() -> {
            discoverBridges();
        });*/

        if (bridgeHandlers.isEmpty()) {
            logger.debug("startScan(): AirZoneDiscoveryService cannot proceed due to missing AirZone bridge(s).");
        } else {
            logger.debug("startScan(): Starting AirZone discovery scan for zones and actuators.");
            discoverZones();
        }
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
    private void discoverZones() {
        logger.trace("discoverZones() called.");
        for (AirZoneBridgeHandler bridgeHandler : bridgeHandlers) {
            ThingUID bridgeUID = bridgeHandler.getThing().getUID();
            logger.debug("discoverZones(): discovering all zones on bridge {}.", bridgeUID);

            AirZoneApiManager apiManager = bridgeHandler.getApiManager();
            apiManager.fetchStatus();
            for (var system : apiManager.getLatestResponse().getSystems()) {
                for (var zone : system.getData()) {
                    String zoneName = zone.getName().toString();
                    logger.trace("discoverZones(): found zone {}.", zoneName);
                    
                    String label = zoneName.replaceAll("\\P{Alnum}", "_");
                    logger.trace("discoverZones(): using label {}.", label);

                    ThingTypeUID thingTypeUID = AirZoneBindingConstants.THING_TYPE_AIRZONE_ZONE;
                    ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, label);
                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                            //.withProperty(AirZoneBindingProperties.PROPERTY_ZONE_NAME, zoneName)
                            //.withRepresentationProperty(AirZoneBindingProperties.PROPERTY_ZONE_NAME)
                            .withBridge(bridgeUID)
                            .withLabel(label).build();
                    logger.debug("discoverZones(): registering new thing {}.", discoveryResult);
                    thingDiscovered(discoveryResult);
                }
            }
        }
        logger.trace("discoverZones() finished.");
    }

    /**
     * Add a {@link AirZoneBridgeHandler} to the {@link AirZoneDiscoveryService}
     *
     * @param bridge AirZone bridge handler.
     * @return true if the bridge was added, or false if it was already present
     */
    public boolean addBridge(AirZoneBridgeHandler bridge) {
        if (!bridgeHandlers.contains(bridge)) {
            logger.trace("AirZoneDiscoveryService(): registering bridge {} for discovery.", bridge);
            bridgeHandlers.add(bridge);
            return true;
        }
        logger.trace("AirZoneDiscoveryService(): bridge {} already registered for discovery.", bridge);
        return false;
    }

    /**
     * Remove a {@link AirZoneBridgeHandler} from the {@link AirZoneDiscoveryService}
     *
     * @param bridge AirZone bridge handler.
     * @return true if the bridge was removed, or false if it was not present
     */
    public boolean removeBridge(AirZoneBridgeHandler bridge) {
        return bridgeHandlers.remove(bridge);
    }

    /**
     * Check if the {@link AirZoneDiscoveryService} list of {@link AirZoneBridgeHandler} is empty
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return bridgeHandlers.isEmpty();
    }
}
