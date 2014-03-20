/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * FloatObjectCacheTest - test for FloatObjectTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class FloatObjectCacheTest {
    public void testCache() {
        FloatObjectStorage cache = new FloatObjectTroveStorage();

        assert cache.size() == 0;

        assert cache.load(42f) == Storage.UNDEFINED;

        cache.save(42f, "123");

        assert cache.size() == 1;
        Assert.assertEquals(cache.load(42f), "123");

        cache.save(42f, null);

        assert cache.size() == 1;
         Assert.assertNull(cache.load(42f));

        cache.clear();

        assert cache.load(42f) == Storage.UNDEFINED;
  }
}
