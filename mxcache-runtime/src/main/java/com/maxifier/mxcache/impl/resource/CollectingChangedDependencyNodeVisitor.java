/* Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 * Maxifier Ltd  proprietary and confidential.
 * Use is subject to license terms.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;
import gnu.trove.THashMap;
import gnu.trove.TObjectIdentityHashingStrategy;

import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (04.06.13)
 */
public class CollectingChangedDependencyNodeVisitor implements DependencyNodeVisitor {
    private static final TObjectIdentityHashingStrategy<ResourceViewable> HASHING_STRATEGY = new TObjectIdentityHashingStrategy<ResourceViewable>();

    private final Set<DependencyNode> nodes;
    private final Queue<DependencyNode> queue;

    private final Map<ResourceViewable, Boolean> changedCache = new THashMap<ResourceViewable, Boolean>(HASHING_STRATEGY);

    public CollectingChangedDependencyNodeVisitor(Set<DependencyNode> nodes, Queue<DependencyNode> queue) {
        this.nodes = nodes;
        this.queue = queue;
    }

    @Override
    public void visit(DependencyNode node) {
        if (node instanceof ResourceViewable) {
            ResourceViewable resourceViewable = (ResourceViewable) node;
            if (nodes.contains(node) || !isChanged(resourceViewable)) {
                return;
            }
        }
        if (nodes.add(node)) {
            queue.add(node);
        }
    }

    private boolean isChanged(ResourceViewable resourceViewable) {
        Boolean res = changedCache.get(resourceViewable);
        if (res == null) {
            res = resourceViewable.isChanged();
            changedCache.put(resourceViewable, res);
        }
        return res;
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