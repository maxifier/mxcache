/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;

import java.util.*;
import java.lang.ref.WeakReference;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MultipleDependencyNode extends AbstractDependencyNode {
    protected final List<WeakReference<CleaningNode>> instances = new ArrayList<WeakReference<CleaningNode>>();

    @Override
    public synchronized void addNode(@Nonnull CleaningNode node) {
        instances.add(new WeakReference<CleaningNode>(node));
    }

    @Override
    public synchronized void invalidate() {
        for (Iterator<WeakReference<CleaningNode>> it = instances.iterator(); it.hasNext();) {
            WeakReference<CleaningNode> ref = it.next();
            CleaningNode node = ref.get();
            if (node != null) {
                node.invalidate();
            } else {
                it.remove();
            }
        }
    }

    @Override
    public synchronized String toString() {
        StringBuilder b = new StringBuilder("DependencyNode<");
        int gc = 0;
        int n = 0;
        for (WeakReference<CleaningNode> instance : instances) {
            CleaningNode node = instance.get();
            if (node == null) {
                gc++;
            } else {
                n++;
                b.append(node.toString()).append(", ");
            }
        }
        if (gc > 0) {
            b.append(gc).append(" GCed");
        } else if (n > 0) {
            b.setLength(b.length() - 2);
        }
        b.append(">");
        return b.toString();
    }
}
