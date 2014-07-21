/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.LongBooleanStorage;
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
public class LongBooleanCacheTest {
    public void testCache() {
        LongBooleanStorage cache = new LongBooleanTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42L);

        cache.save(42L, true);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42L), true);

        cache.clear();

        assert !cache.isCalculated(42L);
    }
}
