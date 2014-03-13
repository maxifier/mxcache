/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.IntShortStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * IntShortCacheTest - test for IntShortTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class IntShortCacheTest {
    public void testCache() {
        IntShortStorage cache = new IntShortTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42);

        cache.save(42, (short)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42), (short)42);

        cache.clear();

        assert !cache.isCalculated(42);
    }
}
