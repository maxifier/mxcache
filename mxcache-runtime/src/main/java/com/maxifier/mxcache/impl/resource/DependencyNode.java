/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.util.Queue;

/**
 * Dependency node keeps track about dependencies of cache. It gathers dependency info in runtime.
 *
 * Each cache has a DependencyNode associated with it. The dependency node may be shared across few cache instances
 * (e.g. all instances of certain method share a single dependency node) or created for each cache instance
 * individually. DependencyNodes form a directed graph where edge from A to B means that all caches associated with B
 * depend on caches associated with A and should be invalidated once any of caches associated with A are invalidated.
 *
 * @see com.maxifier.mxcache.impl.resource.DependencyTracker
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface DependencyNode {
    /**
     * Each node must maintain a reference to itself. This reference should have equals and hashCode implemented to
     * compare corresponding referent objects. This reference is used when adding a dependency to node.
     * @return reference to this node.
     */
    Reference<DependencyNode> getSelfReference();

    /**
     * Should pass all dependent nodes to visitor.
     * @param visitor visitor
     */
    void visitDependantNodes(Visitor visitor);

    /**
     * Invalidates all the caches belonging to this node.
     */
    void invalidate();

    /**
     * Adds a dependency on a given node.
     * @param node new dependency node
     */
    void trackDependency(DependencyNode node);

    /**
     * Adds a cache that should be managed by this node
     * @param cache cache
     */
    void addNode(@Nonnull CleaningNode cache);

    interface Visitor {
        Queue<DependencyNode> getQueue();

        void visit(DependencyNode t);
    }
}
