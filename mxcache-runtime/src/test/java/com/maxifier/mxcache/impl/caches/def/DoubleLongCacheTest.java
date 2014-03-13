/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.DoubleLongStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * DoubleLongCacheTest - test for DoubleLongTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class DoubleLongCacheTest {
    public void testCache() {
        DoubleLongStorage cache = new DoubleLongTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated(42d);

        cache.save(42d, 42L);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42d), 42L);

        cache.clear();

        assert !cache.isCalculated(42d);
    }
}
