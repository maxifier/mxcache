package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.PublicAPI;

import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 9:39:24
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
