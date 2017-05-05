/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;

import java.util.List;

/**
 * ClassCacheIds
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ClassCacheIds {
    private final int[] instanceIds;
    private final int[] staticIds;

    public ClassCacheIds(int[] instanceIds, int[] staticIds) {
        this.instanceIds = instanceIds;
        this.staticIds = staticIds;
    }

    public int[] getInstanceIds() {
        return instanceIds;
    }

    public int[] getStaticIds() {
        return staticIds;
    }

    @SuppressWarnings({ "unchecked" })
    void appendInstanceCaches(Cleanable cleanable, Object instance, List<Cache> list) {
        for (int id : instanceIds) {
            list.add(cleanable.getInstanceCache(instance, id));
        }
    }
}
