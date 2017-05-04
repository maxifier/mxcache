/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guava;

import static com.maxifier.mxcache.impl.caches.def.TroveHelper.boxNull;
import static com.maxifier.mxcache.impl.caches.def.TroveHelper.unboxNull;

import com.google.common.cache.*;
import com.google.common.util.concurrent.ExecutionError;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.AbstractCacheManager;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.elementlocked.AbstractObjectObjectCache;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.storage.Storage;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * GuavaCacheStrategy
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-08 18:15)
 * @author Aleksey Dergunov (aleksey.dergunov@maxifier.com) (2013-09-06 17:39)
 */
@ParametersAreNonnullByDefault
public class GuavaCacheStrategy implements CachingStrategy {
    private static final StrategyProperty<Long> MAXIMUM_SIZE = StrategyProperty.create("guava.maxSize", long.class, UseGuava.class, "maxSize");
    private static final StrategyProperty<Long> MAXIMUM_WEIGHT = StrategyProperty.create("guava.maxWeight", long.class, UseGuava.class, "maxWeight");
    private static final StrategyProperty<Integer> INITIAL_CAPACITY = StrategyProperty.create("guava.initialCapacity", int.class, UseGuava.class, "initialCapacity");
    private static final StrategyProperty<Integer> CONCURRENCY_LEVEL = StrategyProperty.create("guava.concurrencyLevel", int.class, UseGuava.class, "concurrencyLevel");
    private static final StrategyProperty<GuavaOption[]> OPTIONS = StrategyProperty.create("guava.option", GuavaOption[].class, UseGuava.class, "options");
    private static final StrategyProperty<Long> EXPIRE_AFTER_ACCESS = StrategyProperty.create("guava.expireAfterAccess", long.class, UseGuava.class, "expireAfterAccess");
    private static final StrategyProperty<Long> EXPIRE_AFTER_WRITE = StrategyProperty.create("guava.expireAfterWrite", long.class, UseGuava.class, "expireAfterWrite");
    private static final StrategyProperty<Long> REFRESH_AFTER_WRITE = StrategyProperty.create("guava.refreshAfterWrite", long.class, UseGuava.class, "refreshAfterWrite");
    private static final StrategyProperty<Class> WEIGHER_CLASS = StrategyProperty.create("guava.weigher", Class.class, UseGuava.class, "weigher");

    @Nonnull
    @Override
    public CacheManager getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        return new GuavaCacheManager(context, ownerClass, descriptor);
    }

    private static class GuavaCacheManager extends AbstractCacheManager {
        public GuavaCacheManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
            super(context, ownerClass, descriptor);
        }

        @Nonnull
        @Override
        protected com.maxifier.mxcache.caches.Cache createCache(Object owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException {
            return createCache0(owner, dependencyNode);
        }

        @SuppressWarnings("unchecked")
        private <K, V> com.maxifier.mxcache.caches.Cache createCache0(Object owner, DependencyNode dependencyNode) {
            CacheDescriptor descriptor = getDescriptor();
            ObjectObjectCalculatable<K, V> calculableWrapper = Wrapping.getCalculableWrapper(descriptor.getSignature().erased(), descriptor.getCalculable());

            CacheLoader loader = new GuavaCacheLoader(owner, calculableWrapper);
            CacheBuilder<Object, Object> builder = CacheBuilder.newBuilder();
            Long maxSize = descriptor.getProperty(MAXIMUM_SIZE);
            if (maxSize != null && maxSize > 0) {
                builder.maximumSize(maxSize);
            }
            Integer concurrencyLevel = descriptor.getProperty(CONCURRENCY_LEVEL);
            if (concurrencyLevel != null && concurrencyLevel > 0) {
                builder.concurrencyLevel(concurrencyLevel);
            }
            GuavaOption[] options = descriptor.getProperty(OPTIONS);
            if (options != null) {
                for (GuavaOption option : options) {
                    option.set(builder);
                }
            }
            Integer initialCapacity = descriptor.getProperty(INITIAL_CAPACITY);
            if (initialCapacity != null && initialCapacity > 0) {
                builder.initialCapacity(initialCapacity);
            }
            Long expireAfterAccess = descriptor.getProperty(EXPIRE_AFTER_ACCESS);
            if (expireAfterAccess != null && expireAfterAccess > 0) {
                builder.expireAfterAccess(expireAfterAccess, TimeUnit.MILLISECONDS);
            }
            Long expireAfterWrite = descriptor.getProperty(EXPIRE_AFTER_WRITE);
            if (expireAfterWrite != null && expireAfterWrite > 0) {
                builder.expireAfterWrite(expireAfterWrite, TimeUnit.MILLISECONDS);
            }
            Long refreshAfterWrite = descriptor.getProperty(REFRESH_AFTER_WRITE);
            if (refreshAfterWrite != null && refreshAfterWrite > 0) {
                builder.refreshAfterWrite(refreshAfterWrite, TimeUnit.MILLISECONDS);
            }
            Class<? extends Weigher> weighterClass = descriptor.getProperty(WEIGHER_CLASS);
            if (weighterClass != null && weighterClass != UseGuava.NoWeigher.class) {
                builder.weigher(getContext().getInstanceProvider().forClass(weighterClass));
            }
            Long maxWeight = descriptor.getProperty(MAXIMUM_WEIGHT);
            if (maxWeight != null && maxWeight > 0) {
                builder.maximumWeight(maxWeight);
            }
            LoadingCache<K, V> cache = builder.build(loader);

            return Wrapping.getObjectObjectCacheWrapper(new GuavaCacheWrapper<K, V>(owner, calculableWrapper, cache, dependencyNode, descriptor));
        }

        @Override
        public String getImplementationDetails() {
            return "GuavaCache";
        }
    }

    private static class GuavaCacheLoader<K, V> extends CacheLoader<K, V> {
        private final Object owner;
        private final ObjectObjectCalculatable<K, V> calculable;

        public GuavaCacheLoader(Object owner, ObjectObjectCalculatable<K, V> calculable) {
            this.owner = owner;
            this.calculable = calculable;
        }

        @Override
        public V load(K key) throws Exception {
            return boxNull(calculable.calculate(owner, unboxNull(key)));
        }
    }

    private static class GuavaCacheWrapper<K, V> extends AbstractObjectObjectCache<K, V> {
        private final LoadingCache<K, V> cache;
        private final DependencyNode dependencyNode;
        private final ReadWriteLock readWriteLock;
        private final CacheDescriptor descriptor;

        public GuavaCacheWrapper(Object owner, ObjectObjectCalculatable<K, V> calculatable, LoadingCache<K, V> cache, DependencyNode dependencyNode, CacheDescriptor descriptor) {
            super(owner, calculatable, null);
            this.cache = cache;
            this.dependencyNode = dependencyNode;
            this.descriptor = descriptor;
            this.readWriteLock = new ReentrantReadWriteLock();
        }

        @Override
        protected V create(K key) {
            try {
                return unboxNull(cache.get(boxNull(key)));
            } catch (ExecutionException e) {
                throw new RuntimeException(e.getCause());
            } catch (UncheckedExecutionException e) {
                throw (RuntimeException) e.getCause();
            } catch (ExecutionError e) {
                throw (Error) e.getCause();
            }
        }

        @Override
        public void lock(K key) {
            // we don't use parameter key - guava makes locking by element by itself
            readWriteLock.readLock().lock();
        }

        @Override
        public void unlock(K key) {
            // we don't use parameter key - guava makes unlocking by element by itself
            readWriteLock.readLock().unlock();
        }

        @Override
        public Object load(K key) {
            Object value = cache.getIfPresent(boxNull(key));
            if (value == null) {
                return Storage.UNDEFINED;
            }
            return unboxNull(value);
        }

        @Override
        public void save(K key, Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return (int) cache.size();
        }

        @Override
        public int getSize() {
            return (int) cache.size();
        }

        @Override
        public void miss(long dt) {
            // empty - guava gathers all statistics
        }

        @Override
        public void hit() {
            // empty - guava gathers all statistics
        }

        @Override
        public CacheDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public void setDependencyNode(DependencyNode dependencyNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Lock getLock() {
            return readWriteLock.writeLock();
        }

        @Override
        public void clear() {
            cache.invalidateAll();
            // invalidateAll doesn't actually clear all the caches, it only guarantees that no old (dirty) data may
            // be read back from cache after invalidation. Actually, the references to old keys/values are still
            // retained in recencyQueue in case of limited cache which leads to a memory leak: recency queues are
            // per cache segment, so if no one reads certain segment, the queue never gets drained and thus a
            // reference to an old object is retained forever.
            cache.cleanUp();
        }

        @Override
        public DependencyNode getDependencyNode() {
            return dependencyNode;
        }

        @Override
        public Statistics getStatistics() {
            return new StatisticsWrapper(cache.stats());
        }
    }

    private static class StatisticsWrapper implements Statistics {
        private final CacheStats stats;

        public StatisticsWrapper(CacheStats stats) {
            this.stats = stats;
        }

        @Override
        public int getHits() {
            return (int) stats.hitCount();
        }

        @Override
        public int getMisses() {
            return (int) stats.missCount();
        }

        @Override
        public long getTotalCalculationTime() {
            return stats.totalLoadTime();
        }

        @Override
        public double getAverageCalculationTime() {
            return stats.averageLoadPenalty();
        }

        @Override
        public void reset() {
            // unsupported
        }
    }
}
