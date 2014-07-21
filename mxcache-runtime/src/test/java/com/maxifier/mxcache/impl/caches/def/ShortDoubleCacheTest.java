/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ShortDoubleStorage;
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
public class ShortDoubleCacheTest {
    public void testCache() {
        ShortDoubleStorage cache = new ShortDoubleTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((short)42);

        cache.save((short)42, 42d);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((short)42), 42d);

        cache.clear();

        assert !cache.isCalculated((short)42);
    }
}
