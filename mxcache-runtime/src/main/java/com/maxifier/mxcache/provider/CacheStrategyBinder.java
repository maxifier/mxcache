package com.maxifier.mxcache.provider;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.06.2010
 * Time: 11:13:51
 */
public interface CacheStrategyBinder<T extends CachingStrategy> {
    void to(T instance);
}
