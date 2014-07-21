/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectBooleanStorage;
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
public class ObjectBooleanCacheTest {
    public void testCache() {
        ObjectBooleanStorage cache = new ObjectBooleanTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated("123");

        cache.save("123", true);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load("123"), true);

        cache.clear();

        assert !cache.isCalculated("123");
    }
}
