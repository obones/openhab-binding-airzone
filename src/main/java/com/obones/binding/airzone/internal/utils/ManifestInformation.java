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
package com.obones.binding.airzone.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.osgi.framework.FrameworkUtil;

/**
 * This is a helper class for dealing with information from MANIFEST file.
 *
 * It provides the following methods:
 * <ul>
 * <li>{@link #getBundleVersion} returns the bundle version as specified within the MANIFEST.</li>
 * </ul>
 * <p>
 *
 * @author Olivier Sannier - Initial contribution
 */
@NonNullByDefault
public class ManifestInformation {

    /*
     * ************************
     * ***** Constructors *****
     */

    /**
     * Suppress default constructor for creating a non-instantiable class.
     */
    private ManifestInformation() {
        throw new AssertionError();
    }

    // Class access methods

    /**
     * Returns the bundle version as specified within the MANIFEST file.
     *
     * @return <B>bundleVersion</B> the resulted bundle version as {@link String}.
     */
    public static String getBundleVersion() {
        String osgiBundleVersion = FrameworkUtil.getBundle(ManifestInformation.class).getBundleContext().getBundle()
                .toString();
        return osgiBundleVersion;
    }
}
