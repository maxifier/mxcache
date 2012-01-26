package com.maxifier.mxcache.context;

import com.maxifier.mxcache.InstanceProvider;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.02.11
 * Time: 16:53
 */
public interface CacheContext {
    InstanceProvider getInstanceProvider();
    
    <T> T getRelated(ContextRelatedItem<T> item);
    
    <T> void setRelated(ContextRelatedItem<T> item, T value);

    @SuppressWarnings("UnusedDeclaration")
    interface ContextRelatedItem<T> {}
}
