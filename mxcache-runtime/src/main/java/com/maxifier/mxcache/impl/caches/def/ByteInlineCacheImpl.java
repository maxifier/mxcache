/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.abs.*;

/**
 * ByteInlineCacheImpl
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ByteInlineCacheImpl extends AbstractByteCache {
    private volatile boolean set;
    private byte value;

    public ByteInlineCacheImpl(Object owner, ByteCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
    }

    @Override
    public boolean isCalculated() {
        return set;
    }

    @Override
    public byte load() {
        return value; 
    }

    @Override
    public void save(byte v) {
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
