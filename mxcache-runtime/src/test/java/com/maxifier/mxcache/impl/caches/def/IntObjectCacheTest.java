/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * IntObjectCacheTest - test for IntObjectTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2OCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class IntObjectCacheTest {
    public void testCache() {
        IntObjectStorage cache = new IntObjectTroveStorage();

        assert cache.size() == 0;

        assert cache.load(42) == Storage.UNDEFINED;

        cache.save(42, "123");

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42), "123");

        cache.save(42, null);

        assert cache.size() == 1;
         Assert.assertNull(cache.load(42));

        cache.clear();

        assert cache.load(42) == Storage.UNDEFINED;
  }
}
