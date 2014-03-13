/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ByteByteStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * ByteByteCacheTest - test for ByteByteTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ByteByteCacheTest {
    public void testCache() {
        ByteByteStorage cache = new ByteByteTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((byte)42);

        cache.save((byte)42, (byte)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((byte)42), (byte)42);

        cache.clear();

        assert !cache.isCalculated((byte)42);
    }
}
