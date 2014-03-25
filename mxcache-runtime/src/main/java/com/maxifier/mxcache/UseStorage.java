/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.storage.Storage;

import java.lang.annotation.*;

/**
 * Specifies which storage class to use for your @Cached method.
 *
 * @see com.maxifier.mxcache.UseStorageFactory
 * @see com.maxifier.mxcache.Strategy
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseStorage {
    Class<? extends Storage> value();
}
