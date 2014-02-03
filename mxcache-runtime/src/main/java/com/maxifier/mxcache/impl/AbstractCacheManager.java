package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.resource.MxResource;
import javax.annotation.Nullable;

import javax.annotation.Nonnull;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 27.04.2010
 * Time: 19:15:05
 */
public abstract class AbstractCacheManager<T> implements CacheManager<T> {
    private static final DependencyTracking DEFAULT_DEPENDENCY_TRACKING_VALUE = DependencyTracking.INSTANCE;
    private static final StatisticsModeEnum DEFAULT_STATISTICS_MODE = StatisticsModeEnum.STATIC_OR_STORAGE;

    private final CacheDescriptor<T> descriptor;

    private final DependencyNode staticNode;

    private final DependencyTracking trackDependency;
    private final MxResource[] resourceDependencies;

    private final CacheContext context;

    private final StatisticsModeEnum statisticsMode;

    private final MutableStatistics staticStatistics;

    public AbstractCacheManager(CacheContext context, CacheDescriptor<T> descriptor) {
        this.descriptor = descriptor;
        this.context = context;

        statisticsMode = descriptor.getStatisticsMode() == null ? DEFAULT_STATISTICS_MODE : descriptor.getStatisticsMode();

        switch (statisticsMode) {
            case STATIC:
            case STATIC_OR_STORAGE:
                staticStatistics = new MutableStatisticsImpl();
                break;
            case INSTANCE:
            case STORAGE:
                staticStatistics = null;
                break;
            default:
                throw new UnsupportedOperationException("Unknown statistics mode: " + statisticsMode);
        }

        trackDependency = convertStatic(convertDefault(descriptor.getTrackDependency()));
        resourceDependencies = getResources(descriptor.getResourceDependencies());

        switch (trackDependency) {
            case NONE:
                if (resourceDependencies == null) {
                    staticNode = null;
                } else {
                    staticNode = createStaticNode();
                }
                break;
            case STATIC:
                staticNode = createStaticNode();
                break;
            case INSTANCE:
                staticNode = null;
                break;
            default:
                throw new UnsupportedOperationException("Unknown value: " + trackDependency);
        }
    }

    private MxResource[] getResources(Set<String> resourceNames) {
        if (resourceNames == null || resourceNames.isEmpty()) {
            return null;
        }
        MxResource[] res = new MxResource[resourceNames.size()];
        int i = 0;
        for (String resourceName : resourceNames) {
            res[i++] = MxResourceFactory.getResource(resourceName);
        }
        return res;
    }

    protected StatisticsModeEnum getStatisticsMode() {
        return statisticsMode;
    }

    /**
     * Создает и регистрирует узел для статических зависимостей. Должен зарегистрировать явные зависимости с помощью registerExplicitDependencies.
     *
     * @return узел зависимостей
     */
    protected DependencyNode createStaticNode() {
        DependencyNode node;
        if (descriptor.isStatic()) {
            node = Wrapping.getSingletonNode(descriptor);
        } else {
            node = Wrapping.getMultipleNode(descriptor);
        }
        registerExplicitDependencies(node);
        return node;
    }

    /**
     * Создает узел зависимостей для отдельного экземпляра. Должен зарегистрировать явные зависимости с помощью registerExplicitDependencies.
     *
     * @return узел
     */
    protected DependencyNode createInstanceNode() {
        DependencyNode node = Wrapping.getSingletonNode(descriptor);
        registerExplicitDependencies(node);
        return node;
    }

    private DependencyTracking convertStatic(DependencyTracking tracking) {
        if (descriptor.isStatic() && tracking == DependencyTracking.INSTANCE) {
            return DependencyTracking.STATIC;
        }
        return tracking;
    }

    private static DependencyTracking convertDefault(DependencyTracking tracking) {
        return tracking == DependencyTracking.DEFAULT ? DEFAULT_DEPENDENCY_TRACKING_VALUE : tracking;
    }

    /**
     * Добавляет статические зависимости от ресурсов, заданные через аннотации
     *
     * @param node ухед зависимостей
     */
    protected void registerExplicitDependencies(DependencyNode node) {
        if (resourceDependencies != null) {
            for (MxResource resourceId : resourceDependencies) {
                DependencyTracker.addExplicitDependency(node, resourceId);
            }
        }
    }

    @Override
    public Cache createCache(@Nullable T owner) {
        if (descriptor.isStatic()) {
            if (owner != null) {
                throw new IllegalArgumentException("Static cache " + this + " requires no instance");
            }
        } else if (owner == null) {
            throw new IllegalArgumentException("Non-static cache " + this + " requires instance");
        }
        try {
            DependencyNode dependencyNode = getDependencyNode();
            Cache instance = createCache(owner, trackDependency == DependencyTracking.NONE ? DependencyTracker.DUMMY_NODE : dependencyNode, createStatistics());
            if (dependencyNode != null) {
                dependencyNode.addNode(instance);
            }
            return ProxyingCacheGenerator.wrapCacheWithProxy(descriptor, context, instance);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create cache", e);
        }
    }

    private MutableStatistics createStatistics() {
        switch (statisticsMode) {
            case STATIC:
            case STATIC_OR_STORAGE:
                return staticStatistics;
            case INSTANCE:
                return new MutableStatisticsImpl();
            case STORAGE:
                return null;
            default:
                throw new UnsupportedOperationException("Unsupported statistics mode: " + statisticsMode);
        }
    }

    private DependencyNode getDependencyNode() {
        switch (trackDependency) {
            case NONE:
            case STATIC:
                return staticNode;
            case INSTANCE:
                return createInstanceNode();
            default:
                throw new UnsupportedOperationException("Unknown value: " + trackDependency);
        }
    }

    @Nonnull
    protected abstract Cache createCache(T owner, DependencyNode dependencyNode, MutableStatistics statistics) throws InstantiationException, IllegalAccessException, InvocationTargetException;

    @Override
    public CacheDescriptor<T> getDescriptor() {
        return descriptor;
    }

    @Override
    public String toString() {
        return descriptor.toString();
    }

    @Override
    public CacheContext getContext() {
        return context;
    }
}
