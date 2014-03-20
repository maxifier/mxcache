/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.BooleanCache;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.storage.BooleanStorage;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ViewableSingletonBooleanDependencyNode
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (29.05.13)
 */
public class ViewableSingletonBooleanDependencyNode extends SingletonDependencyNode implements ResourceViewable {
    private static final Logger logger = LoggerFactory.getLogger(ViewableSingletonBooleanDependencyNode.class);

    @Override
    public synchronized void addNode(@Nonnull CleaningNode cache) {
        super.addNode(cache);
        if (!(cache instanceof BooleanStorage)) {
            String owner = "";
            if (cache instanceof Cache) {
                owner = ((Cache) cache).getDescriptor().toString();
            }

            logger.error("@ResourceView is incorrect specified for method {}. Return type {} is not supported by ResourceView", owner, cache.getClass());
        }
    }

    @Override
    public boolean isChanged() {
        if (instance instanceof BooleanStorage && instance instanceof BooleanCache) {
            BooleanStorage storage = (BooleanStorage) instance;
            BooleanCache cache = (BooleanCache) instance;
            DependencyNode prevNode = DependencyTracker.track(DependencyTracker.NOCACHE_NODE);
            try {
                return storage.isCalculated() && !DependencyTracker.isDependentResourceView(cache) && cache.getOrCreate() != storage.load();
            } finally {
                DependencyTracker.exit(prevNode);
            }
        } else {
            return true;
        }
    }
}
