/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface MutableStatistics extends Statistics, StatisticsHolder {
    void hit();

    void miss(long time);
}
