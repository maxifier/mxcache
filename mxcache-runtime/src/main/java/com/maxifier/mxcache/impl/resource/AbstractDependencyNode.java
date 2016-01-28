/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.util.HashWeakReference;
import gnu.trove.set.hash.THashSet;
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
    private Set<Reference<DependencyNode>> dependentNodes;

    /**
     * Number of elements in dependentNodes after which all the set should be checked for the presence of
     * references to GC'ed objects.
     *
     * This threshold is required in order to evict such references as they pollute memory and never GC'ed otherwise.
     */
    private int cleanupThreshold = 10;

    private Reference<DependencyNode> selfReference;

    @Override
    public synchronized void visitDependantNodes(Visitor visitor) {
        if (dependentNodes != null) {
            for (Iterator<Reference<DependencyNode>> it = dependentNodes.iterator(); it.hasNext();) {
                Reference<DependencyNode> ref = it.next();
                DependencyNode instance = ref.get();
                if (instance != null) {
                    visitor.visit(instance);
                } else {
                    it.remove();
                }
            }
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
            dependentNodes = new THashSet<Reference<DependencyNode>>();
        }
        dependentNodes.add(node.getSelfReference());
        cleanupIfNeeded();
    }

    private void cleanupIfNeeded() {
        if (dependentNodes.size() >= cleanupThreshold) {
            for (Iterator<Reference<DependencyNode>> it = dependentNodes.iterator(); it.hasNext(); ) {
                if (it.next().get() == null) {
                    it.remove();
                }
            }
            // It's important to increase cleanup threshold according to the number of elements in a set
            // in order to maintain the balance between CPU-overhead and memory-overhead

            // The cleanup has O(N) complexity, so doing this on addition of N new elements would lead to constant
            // small overhead and thus would not affect the asymptotic behaviour of operations.

            // The memory overhead could be significant but it's guaranteed that memory usage would not be more than
            // 2 * peak memory usage for alive elements.
            cleanupThreshold = dependentNodes.size() * 2;
        }
    }

    protected static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
