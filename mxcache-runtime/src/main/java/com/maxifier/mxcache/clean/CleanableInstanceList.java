package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 31.08.2010
 * Time: 10:54:38
 */
interface CleanableInstanceList {
    int deepLock();

    void deepUnlock();

    void getLists(List<WeakList<?>> lists);

    void getCaches(List<CleaningNode> caches);
}
