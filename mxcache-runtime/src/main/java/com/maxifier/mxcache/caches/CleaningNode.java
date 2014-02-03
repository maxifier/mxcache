package com.maxifier.mxcache.caches;

import com.maxifier.mxcache.impl.resource.DependencyNode;
import javax.annotation.Nullable;

import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.03.11
 * Time: 16:40
 */
public interface CleaningNode {
    /**
     * @return лок, если он есть, null - если нет.
     */
    @Nullable
    Lock getLock();

    /**
     * Очищает кэш.
     */
    void clear();

    DependencyNode getDependencyNode();

    Object getCacheOwner();
}
