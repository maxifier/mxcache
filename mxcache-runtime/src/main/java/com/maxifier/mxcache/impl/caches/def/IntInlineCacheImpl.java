package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;
import com.maxifier.mxcache.impl.resource.DependencyNode;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class IntInlineCacheImpl extends AbstractIntCache {
    private volatile boolean set;
    private int value;

    public IntInlineCacheImpl(Object owner, IntCalculatable calculable, DependencyNode dependencyNode, MutableStatistics statistics) {
        super(owner, calculable, dependencyNode, statistics);
    }

    @Override
    public boolean isCalculated() {
        return set;
    }

    @Override
    public int load() {
        return value; 
    }

    @Override
    public void save(int v) {
        set = true;
        value = v;
    }

    @Override
    public void clear() {
        set = false;
    }

    @Override
    public int size() {
        return set ? 1 : 0;
    }
}
