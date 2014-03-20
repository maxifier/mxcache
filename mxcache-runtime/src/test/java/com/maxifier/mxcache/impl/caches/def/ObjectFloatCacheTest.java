/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectFloatStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * ObjectFloatCacheTest - test for ObjectFloatTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ObjectFloatCacheTest {
    public void testCache() {
        ObjectFloatStorage cache = new ObjectFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated("123");

        cache.save("123", 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load("123"), 42f);

        cache.clear();

        assert !cache.isCalculated("123");
    }
}
