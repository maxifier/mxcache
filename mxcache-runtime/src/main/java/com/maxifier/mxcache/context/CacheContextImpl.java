package com.maxifier.mxcache.context;

import com.maxifier.mxcache.AbstractCacheContext;
import com.maxifier.mxcache.InstanceProvider;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.02.11
 * Time: 8:29
 */
public class CacheContextImpl extends AbstractCacheContext {
    private final InstanceProvider instanceProvider;

    public CacheContextImpl(InstanceProvider instanceProvider) {
        this.instanceProvider = instanceProvider;
    }

    public InstanceProvider getInstanceProvider() {
        return instanceProvider;
    }

    @Override
    public String toString() {
        return "ContextImpl{" + instanceProvider + "}";
    }
}
