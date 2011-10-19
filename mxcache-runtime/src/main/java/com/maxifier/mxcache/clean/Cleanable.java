package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 01.02.2010
 * Time: 15:26:24
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public interface Cleanable<T> {
    void appendStaticCachesTo(List<CleaningNode> locks);

    /**
     * @param id id of cache
     * @return cache
     */
    Cache getStaticCache(int id);

    void appendInstanceCachesTo(List<CleaningNode> locks, T t);

    /**
     * @param t instance of cached class
     * @param id id of cache
     * @return cache
     */
    Cache getInstanceCache(T t, int id);
}
