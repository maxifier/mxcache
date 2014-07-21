/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.BooleanFloatStorage;
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
public class BooleanFloatCacheTest {
    public void testCache() {
        BooleanFloatStorage cache = new BooleanFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(true);

        cache.save(true, 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(true), 42f);

        cache.clear();

        assert !cache.isCalculated(true);
    }
}
