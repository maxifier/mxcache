/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.IntBooleanStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * IntBooleanCacheTest - test for IntBooleanTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class IntBooleanCacheTest {
    public void testCache() {
        IntBooleanStorage cache = new IntBooleanTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42);

        cache.save(42, true);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42), true);

        cache.clear();

        assert !cache.isCalculated(42);
    }
}
