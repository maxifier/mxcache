/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.reflect.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class NonInstrumentedCacheException extends MxCacheException {
    public NonInstrumentedCacheException(Method m) {
        super(m.toString());
    }
}
