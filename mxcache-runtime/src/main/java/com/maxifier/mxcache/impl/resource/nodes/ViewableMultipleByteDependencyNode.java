/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.caches.ByteCache;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.storage.ByteStorage;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * ViewableMultipleByteDependencyNode
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (28.05.13)
 */
public class ViewableMultipleByteDependencyNode extends MultipleDependencyNode implements ResourceViewable {
    private static final Logger logger = LoggerFactory.getLogger(ViewableMultipleByteDependencyNode.class);

    @Override
    public synchronized void addNode(@Nonnull CleaningNode cache) {
        super.addNode(cache);
        if (!(cache instanceof ByteStorage)) {
            String owner = "";
            if (cache instanceof Cache) {
                owner = ((Cache) cache).getDescriptor().toString();
            }

            logger.error("@ResourceView is incorrect specified for method {}. Return type {} is not supported by ResourceView", owner, cache.getClass());
        }
    }

    @Override
    public boolean isChanged() {
        DependencyNode prevNode = DependencyTracker.track(DependencyTracker.NOCACHE_NODE);
        try {
            for (WeakReference<CleaningNode> ref : instances) {
                CleaningNode node = ref.get();
                if (node != null) {
                    if (node instanceof ByteStorage && node instanceof ByteCache) {
                        ByteStorage storage = (ByteStorage) node;
                        ByteCache cache = (ByteCache) node;
                        if (storage.isCalculated() && !DependencyTracker.isDependentResourceView(cache) && cache.getOrCreate() != storage.load()) {
                            return true;
                        }
                    } else {
                        return true;
                    }
                }
            }
        } finally {
            DependencyTracker.exit(prevNode);
        }
        return false;
    }
}
