package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.storage.elementlocked.ObjectObjectElementLockedStorage;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.locking.ExplicitLockingCache;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.03.11
 * Time: 18:33
 */
class EhcacheStorage<E, F> implements ObjectObjectElementLockedStorage<E, F> {
    private final ExplicitLockingCache underlyingCache;

    private final Lock readLock;
    private final Lock writeLock;

    public EhcacheStorage(Cache underlyingCache) {
        this.underlyingCache = new ExplicitLockingCache(underlyingCache);

        ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public void lock(E key) {
        readLock.lock();
        underlyingCache.acquireWriteLockOnKey(key);
    }

    @Override
    public void unlock(E key) {
        underlyingCache.releaseWriteLockOnKey(key);
        readLock.unlock();
    }

    @Override
    public Lock getLock() {
        return writeLock;
    }

    @Override
    public Object load(E key) {
        Element element = underlyingCache.get(key);
        if (element == null) {
            return UNDEFINED;
        }
        return element.getObjectValue();
    }

    @Override
    public void save(E key, F value) {
        underlyingCache.put(new Element(key, value));
    }

    @Override
    public void clear() {
        underlyingCache.removeAll();
    }

    @Override
    public int size() {
        return (int) underlyingCache.getStatistics().getMemoryStoreObjectCount();
    }

    @Override
    public String toString() {
        return underlyingCache.toString();
    }
}
