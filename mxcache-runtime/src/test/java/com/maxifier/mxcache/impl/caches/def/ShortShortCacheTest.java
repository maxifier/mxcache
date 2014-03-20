/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ShortShortStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * ShortShortCacheTest - test for ShortShortTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ShortShortCacheTest {
    public void testCache() {
        ShortShortStorage cache = new ShortShortTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((short)42);

        cache.save((short)42, (short)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((short)42), (short)42);

        cache.clear();

        assert !cache.isCalculated((short)42);
    }
}
