package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.ObjectCalculatable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.AbstractObjectCache;
import com.maxifier.mxcache.impl.resource.DependencyNode;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12/15/11
 * Time: 12:02 PM
 */
public class ObjectInlineCacheImpl<T> extends AbstractObjectCache<T> {
    private volatile Object value = UNDEFINED;

    public ObjectInlineCacheImpl(Object owner, ObjectCalculatable<T> calculable, DependencyNode dependencyNode, MutableStatistics statistics) {
        super(owner, calculable, dependencyNode, statistics);
    }

    @Override
    public Object load() {
        return value;
    }

    @Override
    public void save(T v) {
        value = v;
    }

    @Override
    public void clear() {
        value = UNDEFINED;
    }

    @Override
    public int size() {
        return value == UNDEFINED ? 0 : 1;
    }
}
