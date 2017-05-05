/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.activity;

import gnu.trove.map.hash.THashMap;

import javax.annotation.Nonnull;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ActivityTracker {
    private static final Map<String, Activity> ACTIVITIES = new THashMap<String, Activity>();

//    static {
//        for (ResourceConfig resourceConfig : MxCacheConfig.getInstance().getResources()) {
//            String name = resourceConfig.getName();
//            ACTIVITIES.put(name, new ActivityImpl(name));
//        }
//    }

    private ActivityTracker() {
    }

    /**
     * It is guaranteed that this method will always return the same object for a single id.
     *
     * @param id activity name
     *
     * @return activity with given name
     */
    public static synchronized Activity getActivity(@Nonnull String id) {
        Activity resource = ACTIVITIES.get(id);
        if (resource == null) {
            resource = new ActivityImpl(id);
            ACTIVITIES.put(id, resource);
        }
        return resource;
    }
}
