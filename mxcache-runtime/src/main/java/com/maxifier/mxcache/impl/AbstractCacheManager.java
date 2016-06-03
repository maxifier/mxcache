/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
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

import javax.annotation.Nullable;

import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractCacheManager implements CacheManager {
    private static final DependencyTracking DEFAULT_DEPENDENCY_TRACKING_VALUE = DependencyTracking.INSTANCE;
    private static final AnnotatedDependencyTracking DEFAULT_TRACK_ANNOTATED_DEPENDENCY_VALUE = AnnotatedDependencyTracking.ALL;
    private static final StatisticsModeEnum DEFAULT_STATISTICS_MODE = StatisticsModeEnum.STATIC_OR_STORAGE;

    private final CacheContext context;
    private final Class<?> ownerClass;
    private final CacheDescriptor descriptor;

    private final DependencyNode staticNode;

    private final DependencyTracking trackDependency;
    private final DependencyNode[] explicitDependencies;

    private final StatisticsModeEnum statisticsMode;

    private final MutableStatistics staticStatistics;

    public AbstractCacheManager(CacheContext context, Class<?> ownerClass, CacheDescriptor descriptor) {
        this.context = context;
        this.ownerClass = ownerClass;
        this.descriptor = descriptor;

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
        explicitDependencies = getExplicitDependencies(descriptor, ownerClass, convertDefault(descriptor.getTrackAnnotatedDependency()));

        switch (trackDependency) {
            case NONE:
                if (explicitDependencies == null) {
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

    @Nullable
    private static DependencyNode[] getExplicitDependencies(CacheDescriptor descriptor, Class<?> ownerClass,
                                                            AnnotatedDependencyTracking trackAnnotatedDependency) {
        List<DependencyNode> res = new ArrayList<DependencyNode>();
        Set<String> resourceNames = descriptor.getResourceDependencies();
        if (resourceNames != null) {
            for (String resourceName : resourceNames) {
                res.add((DependencyNode) MxResourceFactory.getResource(resourceName));
            }
        }
        String[] tags = descriptor.getTags();
        if (tags != null) {
            for (String tag : tags) {
                if (trackAnnotatedDependency == AnnotatedDependencyTracking.ALL || !tag.startsWith("@")) {
                    res.add(CacheFactory.getTagDependencyNode(tag));
                }
            }
        }
        String group = descriptor.getGroup();
        if (group != null) {
            res.add(CacheFactory.getGroupDependencyNode(group));
        }
        if (descriptor.isStatic()) {
            // static cache is not invalidated when a superclass of declaring class is invalidated,
            // so we put it to a separate node
            res.add(CacheFactory.getClassDependencyNode(ownerClass));
        } else {
            res.add(CacheFactory.getClassInstanceDependencyNode(ownerClass));
        }
        return res.isEmpty() ? null : res.toArray(new DependencyNode[res.size()]);
    }

    protected StatisticsModeEnum getStatisticsMode() {
        return statisticsMode;
    }

    /**
     * Creates static dependency node and registers it's resource dependencies with registerExplicitDependencies.
     *
     * @return created dependency node
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
     * Creates instance dependency node. Must register explicit resource dependencies with registerExplicitDependencies.
     *
     * @return created dependency node
     */
    protected DependencyNode createInstanceNode() {
        DependencyNode node = Wrapping.getSingletonNode(descriptor);
        registerExplicitDependencies(node);
        return node;
    }

    private DependencyTracking convertStatic(DependencyTracking tracking) {
        if (descriptor.isResourceView()) {
            return DependencyTracking.INSTANCE;
        }
        if (descriptor.isStatic() && tracking == DependencyTracking.INSTANCE) {
            return DependencyTracking.STATIC;
        }
        return tracking;
    }

    private static DependencyTracking convertDefault(DependencyTracking tracking) {
        return tracking == DependencyTracking.DEFAULT ? DEFAULT_DEPENDENCY_TRACKING_VALUE : tracking;
    }

    private static AnnotatedDependencyTracking convertDefault(AnnotatedDependencyTracking trackAnnotatedDependency) {
        return trackAnnotatedDependency == AnnotatedDependencyTracking.DEFAULT ? DEFAULT_TRACK_ANNOTATED_DEPENDENCY_VALUE : trackAnnotatedDependency;
    }

    /**
     * Adds explicit resource dependencies to given node.
     *
     * @param node dependency node
     */
    protected void registerExplicitDependencies(DependencyNode node) {
        if (explicitDependencies != null) {
            for (DependencyNode explicitDependency : explicitDependencies) {
                explicitDependency.trackDependency(node);
            }
        }
    }

    @Override
    public Cache createCache(@Nullable Object owner) {
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
    protected abstract Cache createCache(Object owner, DependencyNode dependencyNode, MutableStatistics statistics) throws Exception;

    @Override
    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    @Override
    public CacheDescriptor getDescriptor() {
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
