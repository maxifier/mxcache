/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectCharacterStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * ObjectCharacterCacheTest - test for ObjectCharacterTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ObjectCharacterCacheTest {
    public void testCache() {
        ObjectCharacterStorage cache = new ObjectCharacterTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated("123");

        cache.save("123", '*');

        assert cache.size() == 1;
        Assert.assertEquals(cache.load("123"), '*');

        cache.clear();

        assert !cache.isCalculated("123");
    }
}
