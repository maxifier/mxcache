/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class CollectingDeepVisitor implements DependencyNode.Visitor {
    private final Set<DependencyNode> visitedNodes;
    private final Queue<DependencyNode> queue;

    public CollectingDeepVisitor(Set<DependencyNode> visitedNodes) {
        this.visitedNodes = visitedNodes;
        this.queue = new LinkedList<DependencyNode>();
    }

    @Override
    public void visit(DependencyNode node) {
        if (visitedNodes.add(node)) {
            queue.add(node);
        }
    }

    @Override
    public Queue<DependencyNode> getQueue() {
        return queue;
    }
}
