package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;
import gnu.trove.THashMap;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 26.01.12
 * Time: 15:46
 */
public abstract class AbstractCacheContext implements CacheContext {
    private final Map<ContextRelatedItem, Object> cache = new THashMap<ContextRelatedItem, Object>();

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T> T getRelated(ContextRelatedItem<T> item) {
        return (T) cache.get(item);
    }

    @Override
    public synchronized <T> void setRelated(ContextRelatedItem<T> item, T value) {
        cache.put(item, value);
    }
}
