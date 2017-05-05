/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.instrumentClass;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CachedVoid {
    @Cached
    public void thisMethodShouldFailToInstrument() {
        
    }
}
