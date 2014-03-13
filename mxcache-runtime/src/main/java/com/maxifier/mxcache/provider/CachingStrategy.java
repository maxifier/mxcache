/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.context.CacheContext;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CachingStrategy {
    /**
     * @param context context
     * @param descriptor дескриптор требуемого менеджера
     * @param <T> тип класса-владельца кэша
     * @return менеджер кэша для заданного дескриптора 
     */
    @Nonnull
    <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor);
}
