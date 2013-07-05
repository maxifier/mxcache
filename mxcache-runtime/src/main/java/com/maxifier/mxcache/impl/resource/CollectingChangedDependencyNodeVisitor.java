/* Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 * Maxifier Ltd  proprietary and confidential.
 * Use is subject to license terms.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;

import java.util.Queue;
import java.util.Set;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (04.06.13)
 */
public class CollectingChangedDependencyNodeVisitor implements DependencyNodeVisitor {
    private final Set<DependencyNode> nodes;
    private final Queue<DependencyNode> queue;

    public CollectingChangedDependencyNodeVisitor(Set<DependencyNode> nodes, Queue<DependencyNode> queue) {
        this.nodes = nodes;
        this.queue = queue;
    }

    @Override
    public void visit(DependencyNode node) {
        if (node instanceof ResourceViewable) {
            ResourceViewable resourceViewable = (ResourceViewable) node;
            if (nodes.contains(node) || !resourceViewable.isChanged()) {
                return;
            }
        }
        if (nodes.add(node)) {
            queue.add(node);
        }
    }

    @Override
    public Set<DependencyNode> getNodes() {
        return nodes;
    }

    @Override
    public Queue<DependencyNode> getQueue() {
        return queue;
    }
}