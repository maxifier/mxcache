package com.maxifier.mxcache.legacy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 04.03.2009
 * Time: 14:52:54
 */
public interface AbstractCacheControl {
    int getHits();

    int getMisses();

    int getTotalSize();

    double getMissRate();

    String printConvertationStatistics();

    long getStateHandlerExecutionTime();
}
