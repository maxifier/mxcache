/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
interface CleanableInstanceList {
    int deepLock();

    void deepUnlock();

    void getLists(List<WeakList<?>> lists);

    void getCaches(List<CleaningNode> caches);
}
