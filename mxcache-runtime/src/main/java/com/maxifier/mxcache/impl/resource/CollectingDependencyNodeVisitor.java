package com.maxifier.mxcache.impl.resource;

import java.util.Queue;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 17.06.11
 * Time: 11:58
 */
class CollectingDependencyNodeVisitor implements DependencyNodeVisitor {
    private final Set<DependencyNode> nodes;
    private final Queue<DependencyNode> queue;

    public CollectingDependencyNodeVisitor(Set<DependencyNode> nodes, Queue<DependencyNode> queue) {
        this.nodes = nodes;
        this.queue = queue;
    }

    @Override
    public void visit(DependencyNode node) {
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
