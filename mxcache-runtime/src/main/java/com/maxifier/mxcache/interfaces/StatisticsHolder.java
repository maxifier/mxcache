/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.interfaces;

import javax.annotation.Nullable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface StatisticsHolder {
    /**
     * This statistics may reflect current state or may refer to the moment when getStatistics() was called.
     * You should never rely on exact values.
     * @return statistics for this cache. May return null if no statistics is available.
     */
    @Nullable
    Statistics getStatistics();
}
