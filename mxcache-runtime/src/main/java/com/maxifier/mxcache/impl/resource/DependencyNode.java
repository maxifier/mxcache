/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.util.Queue;

/**
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
