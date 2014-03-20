/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.LongShortStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * LongShortCacheTest - test for LongShortTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class LongShortCacheTest {
    public void testCache() {
        LongShortStorage cache = new LongShortTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42L);

        cache.save(42L, (short)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42L), (short)42);

        cache.clear();

        assert !cache.isCalculated(42L);
    }
}
