/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNodeVisitor;
import com.maxifier.mxcache.util.HashWeakReference;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Set;

/**
 * CharacterInlineDependencyCache - inline dependency caches are special ones that implement both Cache and DependencyNode.
 * It is used to reduce memory consumption of memory cache for the simplest types of caches.
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CharacterInlineDependencyCache extends CharacterInlineCacheImpl implements DependencyNode {
    /**
     * Set of dependent nodes. It may be null cause there is no need to allocate whole set for each node.
     */
    private Set<Reference<DependencyNode>> dependentNodes;

    private Reference<DependencyNode> selfReference;

    public CharacterInlineDependencyCache(Object owner, CharacterCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
        setDependencyNode(this);
    }

    public synchronized void visitDependantNodes(DependencyNodeVisitor visitor) {
        if (dependentNodes != null) {
            for (Iterator<Reference<DependencyNode>> it = dependentNodes.iterator(); it.hasNext();) {
                Reference<DependencyNode> ref = it.next();
                DependencyNode instance = ref.get();
                if (instance != null) {
                    visitor.visit(instance);
                } else {
                    it.remove();
                }
            }
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
            dependentNodes = new THashSet<Reference<DependencyNode>>();
        }
        dependentNodes.add(node.getSelfReference());
    }

    @Override
    public synchronized void addNode(@Nonnull CleaningNode cache) {
        throw new UnsupportedOperationException("Inline dependency node should has only one cache");
    }

    @Override
    public synchronized void appendNodes(TIdentityHashSet<CleaningNode> elements) {
        elements.add(this);
    }
}
