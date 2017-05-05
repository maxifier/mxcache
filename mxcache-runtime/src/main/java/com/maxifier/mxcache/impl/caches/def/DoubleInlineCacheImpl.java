/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
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
public class DoubleInlineCacheImpl extends AbstractDoubleCache {
    private Object value = UNDEFINED;

    public DoubleInlineCacheImpl(Object owner, DoubleCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
    }

    @Override
    public Object load() {
        return value; 
    }

    @Override
    public void save(Object v) {
        value = v;
    }

    @Override
    public void clear() {
        value = UNDEFINED;
    }

    @Override
    public int size() {
        return value == UNDEFINED ? 0 : 1;
    }
}
