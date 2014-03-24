/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import java.util.regex.Pattern;

/**
 * Moved from ResourceNamingInspection to prevent exception in static initialization section in tests.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ResourceNameChecker {
    private static final Pattern NAME_PATTERN = Pattern.compile("#?([a-zA-Z_][\\w_]*\\.)*([a-zA-Z_][\\w_]*)");

    private ResourceNameChecker() {}

    public static boolean isValidGroupOrTagName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }
}
