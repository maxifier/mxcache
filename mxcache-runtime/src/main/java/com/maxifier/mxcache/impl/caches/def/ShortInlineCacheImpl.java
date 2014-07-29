/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM PInlineCacheImpl.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ShortInlineCacheImpl extends AbstractShortCache {
    private volatile boolean set;
    private short value;

    public ShortInlineCacheImpl(Object owner, ShortCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
    }

    @Override
    public boolean isCalculated() {
        return set;
    }

    @Override
    public short load() {
        return value; 
    }

    @Override
    public void save(short v) {
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
