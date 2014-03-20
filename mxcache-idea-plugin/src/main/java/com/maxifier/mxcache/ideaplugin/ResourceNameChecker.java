/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import java.util.regex.Pattern;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 *
 * Этот класс вынесен из ResourceNamingInspection чтобы его можно было тестировать - иначе падает при статической
 * инициализации.
 */
public final class ResourceNameChecker {
    private static final Pattern NAME_PATTERN = Pattern.compile("#?([a-zA-Z_][\\w_]*\\.)*([a-zA-Z_][\\w_]*)");

    private ResourceNameChecker() {}

    public static boolean isValidGroupOrTagName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }
}
