/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;

/**
 * BooleanInlineCacheImpl
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM PInlineCacheImpl.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class BooleanInlineCacheImpl extends AbstractBooleanCache {
    private volatile boolean set;
    private boolean value;

    public BooleanInlineCacheImpl(Object owner, BooleanCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
    }

    @Override
    public boolean isCalculated() {
        return set;
    }

    @Override
    public boolean load() {
        return value; 
    }

    @Override
    public void save(boolean v) {
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
