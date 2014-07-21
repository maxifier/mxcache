/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.LongFloatStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class LongFloatCacheTest {
    public void testCache() {
        LongFloatStorage cache = new LongFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42L);

        cache.save(42L, 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42L), 42f);

        cache.clear();

        assert !cache.isCalculated(42L);
    }
}
