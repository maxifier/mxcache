package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.03.2010
 * Time: 14:23:14
 */
public class EmptyCleanable<T> implements Cleanable<T> {
    @Override
    public void appendStaticCachesTo(List<CleaningNode> list) {
    }

    @Override
    public Cache getStaticCache(int id) {
        return new CacheWithLock(null);
    }

    @Override
    public void appendInstanceCachesTo(List<CleaningNode> list, T o) {
    }

    @Override
    public Cache getInstanceCache(T o, int id) {
        return new CacheWithLock(null);
    }
}
