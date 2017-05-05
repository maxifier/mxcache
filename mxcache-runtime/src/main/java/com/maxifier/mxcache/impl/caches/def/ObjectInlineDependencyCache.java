/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.DependencyNodes;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.util.HashWeakReference;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Set;

/**
 * Inline dependency caches are special ones that implement both Cache and DependencyNode.
 * It is used to reduce memory consumption of memory cache for the simplest types of caches.
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM PInlineDependencyCache.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectInlineDependencyCache extends ObjectInlineCacheImpl implements DependencyNode {
    /**
     * Set of dependent nodes. It may be null cause there is no need to allocate whole set for each node.
     */
    private DependencyNodes dependentNodes;

    private Reference<DependencyNode> selfReference;

    public ObjectInlineDependencyCache(Object owner, ObjectCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
        setDependencyNode(this);
    }

    @Override
    public synchronized void visitDependantNodes(Visitor visitor) {
        if (dependentNodes != null) {
            dependentNodes.visitDependantNodes(visitor);
        }
    }

    @Override
    public synchronized Reference<DependencyNode> getSelfReference() {
        if (selfReference == null) {
            selfReference = new HashWeakReference<DependencyNode>(this);
        }
        return selfReference;
    }

    @Override
    public synchronized void trackDependency(DependencyNode node) {
        if (dependentNodes == null) {
            dependentNodes =  new DependencyNodes();
        }
        dependentNodes.add(node.getSelfReference());
    }

    @Override
    public void addNode(@Nonnull CleaningNode cache) {
        throw new UnsupportedOperationException("Inline dependency node should has only one cache");
    }
}
