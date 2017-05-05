/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage;

import com.maxifier.mxcache.caches.Calculable;

/**
 * Implement this class in your storage in order to override calculable
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CalculableInterceptor {
    Calculable createInterceptedCalculable(Calculable calculable);
}
