/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CachedInterface {
    @Cached
    public int thisMethodShouldFailToInstrument();
}
