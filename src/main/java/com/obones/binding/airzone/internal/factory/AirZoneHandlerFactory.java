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
package com.obones.binding.airzone.internal.factory;

// @formatter:off
import java.util.HashSet;
//import java.util.Hashtable;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.ServiceRegistration;
//import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.obones.binding.airzone.internal.AirZoneBindingConstants;
import com.obones.binding.airzone.internal.handler.AirZoneBindingHandler;
import com.obones.binding.airzone.internal.handler.AirZoneBridgeHandler;
import com.obones.binding.airzone.internal.handler.AirZoneThingHandler;
import com.obones.binding.airzone.internal.utils.Localization;

/**
 * The {@link AirZoneHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, name = "binding.airzone")
public class AirZoneHandlerFactory extends BaseThingHandlerFactory {
    private @NonNullByDefault({}) final Logger logger = LoggerFactory.getLogger(AirZoneHandlerFactory.class);

    // Class internal

    private @Nullable ServiceRegistration<?> discoveryServiceRegistration = null;
    // private @Nullable AirZoneDiscoveryService discoveryService = null;

    private Set<AirZoneBindingHandler> airZoneBindingHandlers = new HashSet<>();
    private Set<AirZoneBridgeHandler> airZoneBridgeHandlers = new HashSet<>();
    private Set<AirZoneThingHandler> airZoneThingHandlers = new HashSet<>();

    private @NonNullByDefault({}) LocaleProvider localeProvider;
    private @NonNullByDefault({}) TranslationProvider i18nProvider;
    private Localization localization = Localization.UNKNOWN;

    private @Nullable static AirZoneHandlerFactory activeInstance = null;

    // Private

    private void registerDeviceDiscoveryService(AirZoneBridgeHandler bridgeHandler) {
        logger.trace("registerDeviceDiscoveryService({}) called.", bridgeHandler);
        /*
        AirZoneDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService == null) {
            discoveryService = this.discoveryService = new AirZoneDiscoveryService(localization);
        }
        discoveryService.addBridge(bridgeHandler);
        if (discoveryServiceRegistration == null) {
            discoveryServiceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                    discoveryService, new Hashtable<>());
        }
        */
    }

    private synchronized void unregisterDeviceDiscoveryService(AirZoneBridgeHandler bridgeHandler) {
        logger.trace("unregisterDeviceDiscoveryService({}) called.", bridgeHandler);
        /*
        AirZoneDiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.removeBridge(bridgeHandler);
            if (discoveryService.isEmpty()) {
                ServiceRegistration<?> discoveryServiceRegistration = this.discoveryServiceRegistration;
                if (discoveryServiceRegistration != null) {
                    discoveryServiceRegistration.unregister();
                    this.discoveryServiceRegistration = null;
                }
            }
        }
        */
    }

    private @Nullable ThingHandler createBindingHandler(Thing thing) {
        logger.trace("createBindingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        AirZoneBindingHandler airZoneBindingHandler = new AirZoneBindingHandler(thing, localization);
        airZoneBindingHandlers.add(airZoneBindingHandler);
        return airZoneBindingHandler;
    }

    private @Nullable ThingHandler createBridgeHandler(Thing thing) {
        logger.trace("createBridgeHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        AirZoneBridgeHandler airZoneBridgeHandler = new AirZoneBridgeHandler((Bridge) thing, localization);
        airZoneBridgeHandlers.add(airZoneBridgeHandler);
        registerDeviceDiscoveryService(airZoneBridgeHandler);
        return airZoneBridgeHandler;
    }

    private @Nullable ThingHandler createThingHandler(Thing thing) {
        logger.trace("createThingHandler({}) called for thing named '{}'.", thing.getUID(), thing.getLabel());
        AirZoneThingHandler airZoneThingHandler = new AirZoneThingHandler(thing, localization);
        airZoneThingHandlers.add(airZoneThingHandler);
        return airZoneThingHandler;
    }

    private void updateBindingState() {
        airZoneBindingHandlers.forEach((AirZoneBindingHandler airZoneBindingHandler) -> {
            airZoneBindingHandler.updateBindingState(airZoneBridgeHandlers.size(), airZoneThingHandlers.size());
        });
    }

    private void updateLocalization() {
        if (Localization.UNKNOWN.equals(localization) && (localeProvider != null) && (i18nProvider != null)) {
            logger.trace("updateLocalization(): creating Localization based on locale={},translation={}).",
                    localeProvider, i18nProvider);
            localization = new Localization(localeProvider, i18nProvider);
        }
    }

    // Constructor

    @Activate
    public AirZoneHandlerFactory(final @Reference LocaleProvider givenLocaleProvider,
            final @Reference TranslationProvider givenI18nProvider) {
        logger.trace("AirZoneHandlerFactory(locale={},translation={}) called.", givenLocaleProvider, givenI18nProvider);
        localeProvider = givenLocaleProvider;
        i18nProvider = givenI18nProvider;
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

    // Utility methods

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        boolean result = AirZoneBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID)
                || AirZoneBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)
                || AirZoneBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID);
        logger.trace("supportsThingType({}) called and returns {}.", thingTypeUID, result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingHandler resultHandler = null;
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Handle Binding creation
        if (AirZoneBindingConstants.SUPPORTED_THINGS_BINDING.contains(thingTypeUID)) {
            resultHandler = createBindingHandler(thing);
        } else
        // Handle Bridge creation
        if (AirZoneBindingConstants.SUPPORTED_THINGS_BRIDGE.contains(thingTypeUID)) {
            resultHandler = createBridgeHandler(thing);
        } else
        // Handle creation of Things behind the Bridge
        if (AirZoneBindingConstants.SUPPORTED_THINGS_ITEMS.contains(thingTypeUID)) {
            resultHandler = createThingHandler(thing);
        } else {
            logger.warn("createHandler({}) failed: ThingHandler not found for {}.", thingTypeUID, thing.getLabel());
        }
        updateBindingState();
        return resultHandler;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        // Handle Binding removal
        if (thingHandler instanceof AirZoneBindingHandler) {
            logger.trace("removeHandler() removing information element '{}'.", thingHandler.toString());
            airZoneBindingHandlers.remove(thingHandler);
        } else
        // Handle Bridge removal
        if (thingHandler instanceof AirZoneBridgeHandler) {
            logger.trace("removeHandler() removing bridge '{}'.", thingHandler.toString());
            airZoneBridgeHandlers.remove(thingHandler);
            unregisterDeviceDiscoveryService((AirZoneBridgeHandler) thingHandler);
        } else
        // Handle removal of Things behind the Bridge
        if (thingHandler instanceof AirZoneThingHandler) {
            logger.trace("removeHandler() removing thing '{}'.", thingHandler.toString());
            airZoneThingHandlers.remove(thingHandler);
        }
        updateBindingState();
        super.removeHandler(thingHandler);
    }

    @Override
    protected void activate(ComponentContext componentContext) {
        activeInstance = this;
        super.activate(componentContext);
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        activeInstance = null;
        super.deactivate(componentContext);
    }

    public static void refreshBindingInfo() {
        AirZoneHandlerFactory instance = AirZoneHandlerFactory.activeInstance;
        if (instance != null) {
            instance.updateBindingState();
        }
    }
}
