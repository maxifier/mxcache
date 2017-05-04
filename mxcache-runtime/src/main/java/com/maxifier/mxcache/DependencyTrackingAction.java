/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.*;

import javax.annotation.Nonnull;

import java.util.concurrent.Callable;

/**
 * DependencyTrackingAction - use this action if you need to maintain you own cache.
 *
 * To do this, you need to:
 * <ul>
 *     <li>Create an instance of DependencyTrackingAction. Take care of this instance: it should not be GC'ed!</li>
 *     <li>Wrap all your code that creates dependencies to Callable or Runnable and pass it to trackDependencies() of instance</li>
 * </ul>
 *
 * This class may be also used to prevent deadlocks. If the code you invoke with trackDependencies makes use of any
 * resource which is being written at the moment MxCache will unroll the stack by throwing ResourceOccupied error
 * and then it will apply your change again.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-26 15:44)
 */
public class DependencyTrackingAction {
    private final DependencyNode dependencyNode = new DependencyNodeImpl();

    public <T> T trackDependencies(Callable<T> callable) throws Exception {
        while (true) {
            DependencyNode oldNode = null;
            try {
                oldNode = DependencyTracker.track(dependencyNode);
                try {
                    return callable.call();
                } finally {
                    DependencyTracker.exit(oldNode);
                }
            } catch (ResourceOccupied e) {
                if (oldNode != null) {
                    throw e;
                }
                e.getResource().waitForEndOfModification();
            }
        }
    }

    public <T> T trackDependencies(CallableWithoutExceptions<T> callable) {
        while (true) {
            DependencyNode oldNode = null;
            try {
                oldNode = DependencyTracker.track(dependencyNode);
                try {
                    return callable.call();
                } finally {
                    DependencyTracker.exit(oldNode);
                }
            } catch (ResourceOccupied e) {
                if (oldNode != null) {
                    throw e;
                }
                e.getResource().waitForEndOfModification();
            }
        }
    }

    public void trackDependencies(Runnable callable) {
        while (true) {
            DependencyNode oldNode = null;
            try {
                oldNode = DependencyTracker.track(dependencyNode);
                try {
                    callable.run();
                    return;
                } finally {
                    DependencyTracker.exit(oldNode);
                }
            } catch (ResourceOccupied e) {
                if (oldNode != null) {
                    throw e;
                }
                e.getResource().waitForEndOfModification();
            }
        }
    }

    public void mark() {
        DependencyTracker.mark(dependencyNode);
    }

    /** Override this method if you want to clear any caches */
    protected void onClear() {
        // do nothing
    }

    private class DependencyNodeImpl extends AbstractDependencyNode {
        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void invalidate() {
            onClear();
        }
    }
}
