/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CachedNative {
    @Cached
    public native int thisMethodShouldFailToInstrument();
}
