/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.caches.BooleanCache;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.storage.BooleanStorage;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM ViewableMultipleP2PDependencyNode.template
 *
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (28.05.13)
 */
public class ViewableMultipleBooleanDependencyNode extends MultipleDependencyNode implements ResourceViewable {
    private static final Logger logger = LoggerFactory.getLogger(ViewableMultipleBooleanDependencyNode.class);

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
        DependencyNode prevNode = DependencyTracker.track(DependencyTracker.NOCACHE_NODE);
        try {
            for (WeakReference<CleaningNode> ref : instances) {
                CleaningNode node = ref.get();
                if (node != null) {
                    if (node instanceof BooleanStorage && node instanceof BooleanCache) {
                        BooleanStorage storage = (BooleanStorage) node;
                        BooleanCache cache = (BooleanCache) node;
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
