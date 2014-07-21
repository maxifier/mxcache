/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ByteLongStorage;
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
public class ByteLongCacheTest {
    public void testCache() {
        ByteLongStorage cache = new ByteLongTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((byte)42);

        cache.save((byte)42, 42L);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((byte)42), 42L);

        cache.clear();

        assert !cache.isCalculated((byte)42);
    }
}
