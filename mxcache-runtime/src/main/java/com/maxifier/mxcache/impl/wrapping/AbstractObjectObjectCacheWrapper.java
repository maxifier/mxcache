/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.ObjectObjectCache;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;
import javax.annotation.Nullable;

import java.util.concurrent.locks.Lock;

/**
* AbstractObjectObjectCacheWrapper
*
* @author Aleksey Dergunov (aleksey.dergunov@maxifier.com) (06.09.13 16:31)
*/
public abstract class AbstractObjectObjectCacheWrapper<K, V> implements Cache {
    protected final ObjectObjectCache<K, V> delegate;

    AbstractObjectObjectCacheWrapper(ObjectObjectCache<K, V> delegate) {
        this.delegate = delegate;
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    @Override
    public CacheDescriptor getDescriptor() {
        return delegate.getDescriptor();
    }

    @Override
    public void setDependencyNode(DependencyNode node) {
        delegate.setDependencyNode(node);
    }

    @Nullable
    @Override
    public Lock getLock() {
        return delegate.getLock();
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public DependencyNode getDependencyNode() {
        return delegate.getDependencyNode();
    }

    @Nullable
    @Override
    public Statistics getStatistics() {
        return delegate.getStatistics();
    }
}
