package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import com.maxifier.mxcache.storage.elementlocked.ObjectObjectElementLockedStorage;
import com.maxifier.mxcache.legacy.converters.MxConvertType;
import gnu.trove.THashMap;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 10:27:13
 */
class PooledCache<Key, Value, ElementType extends MxConvertType> implements ObjectObjectElementLockedStorage<Key, Value>, StatisticsHolder {
    private final Map<Key, PooledElement<Value>> cache;

    private final MxCachePoolManager<Value> cacheManager;

    private final ElementType elementType;

    private final MxPooledCacheStrategy<Value, ElementType> strategy;

    @SuppressWarnings("UnusedParameters")
    PooledCache(Object owner, MxCachePoolManager<Value> cacheManager, ElementType elementType, MxPooledCacheStrategy<Value, ElementType> strategy) {
        this.elementType = elementType;
        this.strategy = strategy;
        this.cache = new THashMap<Key, PooledElement<Value>>();
        this.cacheManager = cacheManager;

        cacheManager.registerCache(this);
    }

    public int removeNonConfident() {
        assert cacheManager.isHeldByCurrentThread();
        synchronized(cache) {
            int removed = 0;
            for (Iterator<Map.Entry<Key, PooledElement<Value>>> it = cache.entrySet().iterator(); it.hasNext();) {
                PooledElement<Value> element = it.next().getValue();
                // если элемент заблокирован, значит он где-то редактируется
                if (!element.isLocked() && !element.isConfident() && !element.isInPool()) {
                    it.remove();
                    removed++;
                }
            }
            return removed;
        }
    }

    @Override
    public int size() {
        synchronized(cache) {
            return cache.size();
        }
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public Object load(Key key) {
        return getElement(key).getValue();
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public void save(Key key, Value v) {
        getElement(key).setValue(v);
    }

    private PooledElement<Value> getElement(Key key) {
        synchronized(cache) {
            PooledElement<Value> ref = cache.get(key);
            if (ref == null) {
                ref = new PooledElement<Value>(cacheManager, strategy.getConfidence(elementType), cacheManager.getConverter(elementType));
                cache.put(key, ref);
            }
            return ref;
        }
    }

    @Override
    public void clear() {
        assert cacheManager.isHeldByCurrentThread();
        synchronized(cache) {
            cacheManager.removeFromPools(cache.values());
            cache.clear();
        }
    }

    @Override
    public Statistics getStatistics() {
        return cacheManager.getStatistics();
    }

    @Override
    public void lock(Key key) {
        getElement(key).lock();
    }

    @Override
    public void unlock(Key key) {
        getElement(key).unlock();
    }

    @Override
    public Lock getLock() {
        return cacheManager.getLock().getWholeLock();
    }

    @Override
    public String toString() {
        return "PooledCache{type = " + elementType.toString() + "}";
    }
}

