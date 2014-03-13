/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanByteStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * BooleanByteCacheTest - test for BooleanByteTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class BooleanByteCacheTest {
    public void testCache() {
        BooleanByteStorage cache = new BooleanByteTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(true);

        cache.save(true, (byte)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(true), (byte)42);

        cache.clear();

        assert !cache.isCalculated(true);
    }
}
