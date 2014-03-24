/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.util.TIdentityHashSet;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;

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
    void visitDependantNodes(DependencyNodeVisitor visitor);

    /**
     * Adds all caches that should be cleaned with that node to given list
     * (only by this sole node, not it's transitive dependencies).
     * @param elements list to add caches
     */
    void appendNodes(TIdentityHashSet<CleaningNode> elements);

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
}
