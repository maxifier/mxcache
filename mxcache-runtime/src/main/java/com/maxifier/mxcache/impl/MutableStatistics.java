package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 30.08.11
 * Time: 12:24
 */
public interface MutableStatistics extends Statistics, StatisticsHolder {
    void hit();

    void miss(long time);
}
