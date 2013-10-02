/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;

/**
 * DependencyTrackingAction - use this action if you need to maintain you own cache.
 *
 * To do this, you need to:
 * <ul>
 *     <li>Create an instance of DependencyTrackingAction. Take care of this instance: it should not be GC'ed!</li>
 *     <li>Wrap all your code that creates dependencies to Callable or Runnable and pass it to trackDependencies() of instance</li>
 * </ul>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-26 15:44)
 */
public abstract class DependencyTrackingAction {
    private final DependencyNode dependencyNode = new DependencyNodeImpl();

    public <T> T trackDependencies(Callable<T> callable) throws Exception {
        DependencyNode oldNode = DependencyTracker.track(dependencyNode);
        try {
            return callable.call();
        } finally {
            DependencyTracker.exit(oldNode);
        }
    }

    public void trackDependencies(Runnable callable) {
        DependencyNode oldNode = DependencyTracker.track(dependencyNode);
        try {
            callable.run();
        } finally {
            DependencyTracker.exit(oldNode);
        }
    }

    public void mark() {
        DependencyTracker.mark(dependencyNode);
    }

    protected abstract void onClear();

    private class DependencyNodeImpl extends AbstractDependencyNode implements CleaningNode {
        private final Lock lock = new LightweightLock();

        @Override
        public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
            elements.add(this);
        }

        @Override
        public void addNode(@NotNull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Lock getLock() {
            return lock;
        }

        @Override
        public void clear() {
            onClear();
        }

        @Override
        public DependencyNode getDependencyNode() {
            return this;
        }

        @Override
        public Object getCacheOwner() {
            return null;
        }
    }
}
