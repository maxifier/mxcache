/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.set.hash.THashSet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * ResourceViewDependencyNodeVisitor
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (2013-07-05 12:54)
 */
public class ResourceViewDeepVisitor implements DependencyNode.Visitor {
    private final Set<DependencyNode> visitedNodes = new THashSet<DependencyNode>();
    private final Queue<DependencyNode> queue = new LinkedList<DependencyNode>();
    private final TIdentityHashSet<ResourceViewable> result = new TIdentityHashSet<ResourceViewable>();

    @Override
    public void visit(DependencyNode node) {
        if (visitedNodes.add(node)) {
            if (node instanceof ResourceViewable) {
                result.add((ResourceViewable) node);
            }
            queue.add(node);
        }
    }

    public TIdentityHashSet<ResourceViewable> getResult() {
        return result;
    }

    @Override
    public Queue<DependencyNode> getQueue() {
        return queue;
    }
}
