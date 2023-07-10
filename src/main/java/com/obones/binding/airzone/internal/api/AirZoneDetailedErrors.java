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
package com.obones.binding.airzone.internal.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.obones.binding.airzone.internal.utils.Localization;

/**
 * The {@link AirZoneDetailedErrors} is responsible for providing detailed error messages
 * for the currently documented error codes
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class AirZoneDetailedErrors {

    /**
     * Returns the detailed message associated to the given error code, if any.
     */
    @SuppressWarnings("unused") // the code in the else part is used but the IDE insist on saying it is dead...
    public static @Nullable String getDetailedErrorMessage(@Nullable String errorCode, Localization localization) {
        if (errorCode == null)
            return null;

        @Nullable
        String localizationKey = knownErrorCodes.get(errorCode);
        if (localizationKey != null)
            return localization.getText(localizationKey);
        else
            return null;
    }

    // @formatter:off
    @SuppressWarnings("null")
    private static Map<String, String> knownErrorCodes = Map.ofEntries(
        Map.entry("Error 3", "error.error3.description"),
        Map.entry("Error 4", "error.error4.description"),
        Map.entry("Error 5", "error.error5.description"),
        Map.entry("Error 6", "error.error6.description"),
        Map.entry("Error 7", "error.error7.description"),
        Map.entry("Error 8", "error.error8.description"),
        Map.entry("Error 9", "error.error9.description"),
        Map.entry("Error 11", "error.error11.description"),
        Map.entry("Error 13", "error.error13.description"),
        Map.entry("Error 14", "error.error14.description"),
        Map.entry("Error 15", "error.error15.description"),
        Map.entry("Error 16", "error.error16.description"),
        Map.entry("Error C02", "error.errorC02.description"),
        Map.entry("Error C09", "error.errorC09.description"),
        Map.entry("Error C11", "error.errorC11.description"),
        Map.entry("Error IAQ1", "error.errorIAQ1.description"),
        Map.entry("Error IAQ2", "error.errorIAQ2.description"),
        Map.entry("Error IAQ3", "error.errorIAQ3.description"),
        Map.entry("Error IAQ4", "error.errorIAQ4.description")
    );
    // @formatter:on
}
