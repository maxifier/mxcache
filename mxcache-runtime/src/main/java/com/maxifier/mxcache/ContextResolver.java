/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ContextResolver<T> {
    Class<T> getContextOwnerClass();

    CacheContext getContext(T owner);
}
