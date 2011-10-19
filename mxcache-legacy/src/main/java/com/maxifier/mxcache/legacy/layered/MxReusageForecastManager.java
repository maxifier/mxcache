package com.maxifier.mxcache.legacy.layered;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 06.02.2009
 * Time: 12:27:24
 */
public interface MxReusageForecastManager<T> {
    float getReusageForecast(MxLayeredStrategy<T> strategy);
}
