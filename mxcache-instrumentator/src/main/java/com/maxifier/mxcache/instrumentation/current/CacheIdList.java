package com.maxifier.mxcache.instrumentation.current;

import gnu.trove.TIntArrayList;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 14.04.2010
* Time: 10:03:14
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
