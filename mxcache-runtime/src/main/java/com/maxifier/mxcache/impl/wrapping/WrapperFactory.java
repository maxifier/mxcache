/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.storage.Storage;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface WrapperFactory {
    Cache wrap(Object owner, Calculable calculable, Storage storage, MutableStatistics statistics);
}
