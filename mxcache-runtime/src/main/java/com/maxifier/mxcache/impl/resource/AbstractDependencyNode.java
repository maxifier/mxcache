/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.impl.DependencyNodes;
import com.maxifier.mxcache.util.HashWeakReference;

import javax.annotation.Nullable;

import java.lang.ref.Reference;
import java.util.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AbstractDependencyNode implements DependencyNode {
    /**
     * Set of dependent nodes. It may be null cause there is no need to allocate whole set for each node.
     */
    private DependencyNodes dependentNodes;

    private Reference<DependencyNode> selfReference;

    @Override
    public synchronized void visitDependantNodes(Visitor visitor) {
        if (dependentNodes != null) {
            dependentNodes.visitDependantNodes(visitor);
        }
    }

    /**
     * @return approximate size (may include some dead nodes)
     */
    public synchronized int getApproxSize() {
        return dependentNodes == null ? 0 : dependentNodes.size();
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
            // this magic set is used to prevent memory leaks
            // it cleans up references to GC'ed nodes on rehash
            dependentNodes = new DependencyNodes();
        }
        dependentNodes.add(node.getSelfReference());
    }

    protected static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
