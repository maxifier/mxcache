/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ShortFloatStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * ShortFloatCacheTest - test for ShortFloatTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ShortFloatCacheTest {
    public void testCache() {
        ShortFloatStorage cache = new ShortFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((short)42);

        cache.save((short)42, 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((short)42), 42f);

        cache.clear();

        assert !cache.isCalculated((short)42);
    }
}
