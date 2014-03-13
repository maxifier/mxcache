/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage;

import com.maxifier.mxcache.caches.Calculable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CalculableInterceptor {
    Calculable createInterceptedCalculable(Calculable calculable);
}
