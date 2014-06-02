/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import gnu.trove.list.array.TIntArrayList;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class CacheIdList {
    private final TIntArrayList staticCaches = new TIntArrayList();
    private final TIntArrayList instanceCaches = new TIntArrayList();

    public TIntArrayList getStaticCaches() {
        return staticCaches;
    }

    public TIntArrayList getInstanceCaches() {
        return instanceCaches;
    }
}
