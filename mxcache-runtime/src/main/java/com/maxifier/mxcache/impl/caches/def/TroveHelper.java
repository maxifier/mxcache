/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class TroveHelper {
    public static final Object NULL_REPLACEMENT = new Object() {
        @Override
        public String toString() {
            return "<NULL>";
        }
    };

    private TroveHelper() {}

    public static <T> T unboxNull(T res) {
        //noinspection unchecked
        return res == NULL_REPLACEMENT ? null : res;
    }

    public static <T> T boxNull(T res) {
        //noinspection unchecked
        return res == null ? (T) NULL_REPLACEMENT : res;
    }
}
