/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.PublicAPI;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class BasicTransforms {
    private BasicTransforms(){}

    @PublicAPI
    // used in SoftKey
    public static <T> SoftReference<T> createSoftReference(T t) {
        return new SmartSoftReference<T>(t);
    }

    @PublicAPI
    // used in WeakKey
    public static <T> WeakReference<T> createWeakReference(T t) {
        return new SmartWeakReference<T>(t);
    }
}
