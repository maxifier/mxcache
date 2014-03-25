/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public enum StatisticsModeEnum {
    /** No statistics for your cache */
    NONE,
    /** Gather total statistics for all instances */
    STATIC,
    /** Per-instance statistics */
    INSTANCE,
    /**
     * Use storage-provided statistics.
     * Storage should implement {@link com.maxifier.mxcache.interfaces.StatisticsHolder}
     */
    STORAGE,
    /**
     * Use storage-provided statistics if the storage implements
     * {@link com.maxifier.mxcache.interfaces.StatisticsHolder} or
     * use static statistics otherwise. Used by default
     */
    STATIC_OR_STORAGE
}
