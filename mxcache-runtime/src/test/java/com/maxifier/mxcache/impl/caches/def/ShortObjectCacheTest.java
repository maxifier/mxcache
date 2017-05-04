/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2OCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ShortObjectCacheTest {
    public void testCache() {
        ShortObjectStorage cache = new ShortObjectTroveStorage();

        assert cache.size() == 0;

        assert cache.load((short)42) == Storage.UNDEFINED;

        cache.save((short)42, "123");

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((short)42), "123");

        cache.save((short)42, null);

        assert cache.size() == 1;
         Assert.assertNull(cache.load((short)42));

        cache.clear();

        assert cache.load((short)42) == Storage.UNDEFINED;
  }
}
