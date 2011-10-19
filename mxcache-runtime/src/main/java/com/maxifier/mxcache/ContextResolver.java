package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.03.11
 * Time: 15:49
 */
public interface ContextResolver<T> {
    Class<T> getContextOwnerClass();

    CacheContext getContext(T owner);
}
