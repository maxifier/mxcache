/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.provider.StorageFactory;

import java.lang.annotation.*;

/**
 * This annotation allows you to specify custom storage factory.
 * <p>
 * In order to be used your storage factory should have either a default constructor or
 * public constructor that accepts the following types in any order or only one of them:
 * </p>
 * <ul>
 * <li>{@link com.maxifier.mxcache.provider.CacheDescriptor}</li>
 * <li>{@link com.maxifier.mxcache.context.CacheContext}</li>
 * </ul>
 *
 * <p>
 * E.g. the following signatures are ok:
 * </p>
 * <ul>
 * <li>(empty)</li>
 * <li>CacheDescriptor</li>
 * <li>CacheContext</li>
 * <li>CacheDescriptor, CacheContext</li>
 * <li>CacheContext, CacheDescriptor</li>
 * </ul>
 *
 * @see com.maxifier.mxcache.Strategy
 * @see com.maxifier.mxcache.UseStorage
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseStorageFactory {
    Class<? extends StorageFactory> value();
}
