/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.impl.resource.DependencyNode;

import gnu.trove.set.hash.THashSet;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Created by Aleksey Tomin (aleksey.tomin@cxense.com) (2016-06-06)
 */
public class DependencyNodes extends THashSet<Reference<DependencyNode>> {
    /**
     * After delete (1-COMPACT_THRESHOLD)*100% elemetns from
     */
    private static final double COMPACT_THRESHOLD = 0.7;

    /**
     * Number of elements in dependentNodes after which all the set should be checked for the presence of
     * references to GC'ed objects.
     *
     * This threshold is required in order to evict such references as they pollute memory and never GC'ed otherwise.
     */
    private int cleanupThreshold = 10;

    public synchronized void visitDependantNodes(DependencyNode.Visitor visitor) {
        for (Iterator<Reference<DependencyNode>> it = iterator(); it.hasNext();) {
            Reference<DependencyNode> ref = it.next();
            DependencyNode instance = ref.get();
            if (instance != null) {
                visitor.visit(instance);
            } else {
                it.remove();
            }
        }
    }

    public boolean add(Reference<DependencyNode> reference) {
        boolean result = super.add(reference);
        cleanupIfNeeded();
        return result;
    }

    private void cleanupIfNeeded() {
        if (size() >= cleanupThreshold) {
            int oldSize = size();
            for (Iterator<Reference<DependencyNode>> it = iterator(); it.hasNext(); ) {
                if (it.next().get() == null) {
                    it.remove();
                }
            }
            // after delete elements compact greatly accelerates an element insertion into THashSet
            if (size() < oldSize * COMPACT_THRESHOLD) {
                compact();
            }
            // It's important to increase cleanup threshold according to the number of elements in a set
            // in order to maintain the balance between CPU-overhead and memory-overhead

            // The cleanup has O(N) complexity, so doing this on addition of N new elements would lead to constant
            // small overhead and thus would not affect the asymptotic behaviour of operations.

            // The memory overhead could be significant but it's guaranteed that memory usage would not be more than
            // 2 * peak memory usage for alive elements.
            cleanupThreshold = size() * 2;
        }
    }

}
