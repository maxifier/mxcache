/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.context;

import com.maxifier.mxcache.InstanceProvider;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheContext {
    InstanceProvider getInstanceProvider();
    
    <T> T getRelated(ContextRelatedItem<T> item);
    
    <T> void setRelated(ContextRelatedItem<T> item, T value);

    @SuppressWarnings("UnusedDeclaration")
    interface ContextRelatedItem<T> {}
}
