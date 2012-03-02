package com.maxifier.mxcache.ideaplugin;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 29.02.12
 * Time: 22:55
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
