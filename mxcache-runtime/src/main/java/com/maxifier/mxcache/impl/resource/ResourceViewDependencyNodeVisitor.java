/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;
import gnu.trove.set.hash.THashSet;

import java.util.Queue;
import java.util.Set;

/**
 * ResourceViewDependencyNodeVisitor
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (2013-07-05 12:54)
 */
public class ResourceViewDependencyNodeVisitor implements DependencyNodeVisitor {
    private final Set<DependencyNode> resourceViewableNodes;
    private final Set<DependencyNode> allNodes;
    private final Queue<DependencyNode> queue;

    public ResourceViewDependencyNodeVisitor(Set<DependencyNode> resourceViewableNodes, Queue<DependencyNode> queue) {
        this.resourceViewableNodes = resourceViewableNodes;
        this.allNodes = new THashSet<DependencyNode>();
        this.queue = queue;
    }

    @Override
    public void visit(DependencyNode node) {
        if (node instanceof ResourceViewable) {
            resourceViewableNodes.add(node);
        }
        if (allNodes.add(node)) {
            queue.add(node);
        }
    }

    @Override
    public Set<DependencyNode> getNodes() {
        return resourceViewableNodes;
    }

    @Override
    public Queue<DependencyNode> getQueue() {
        return queue;
    }
}
