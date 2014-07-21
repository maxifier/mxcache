/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.IntFloatStorage;
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
public class IntFloatCacheTest {
    public void testCache() {
        IntFloatStorage cache = new IntFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42);

        cache.save(42, 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42), 42f);

        cache.clear();

        assert !cache.isCalculated(42);
    }
}
