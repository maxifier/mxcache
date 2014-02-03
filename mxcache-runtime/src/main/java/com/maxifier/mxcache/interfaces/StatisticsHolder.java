package com.maxifier.mxcache.interfaces;

import javax.annotation.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.09.2010
 * Time: 17:11:55
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
