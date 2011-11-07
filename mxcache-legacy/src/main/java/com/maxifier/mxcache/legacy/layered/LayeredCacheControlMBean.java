package com.maxifier.mxcache.legacy.layered;

import com.maxifier.mxcache.legacy.AbstractCacheControl;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 04.03.2009
 * Time: 14:52:20
 */
public interface LayeredCacheControlMBean extends AbstractCacheControl {
    @Override
    int getHits();

    @Override
    int getMisses();

    @Override
    double getMissRate();

    @Override
    String printConvertationStatistics();

    String printKeyStatistics();

    @Override
    long getStateHandlerExecutionTime();

    int getShortTimeSize();
}
