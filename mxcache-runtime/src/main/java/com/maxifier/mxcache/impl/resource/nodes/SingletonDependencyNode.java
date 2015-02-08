/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class SingletonDependencyNode extends AbstractDependencyNode {
    // we don't need to store reference here cause this node exists only if object itself is not gc'ed
    protected volatile CleaningNode instance;

    public SingletonDependencyNode() {
        // do nothing - instance is set by addNode.
    }

    public SingletonDependencyNode(CleaningNode instance) {
        this.instance = instance;
    }

    @Override
    public void addNode(@Nonnull CleaningNode cache) {
        if (instance != null) {
            throw new UnsupportedOperationException("Singleton dependency node should has only one cache");
        }
        instance = cache;
    }

    @Override
    public void invalidate() {
        instance.invalidate();
    }

    @Override
    public String toString() {
        return "DependencyNode<" + instance + ">";
    }
}
