/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.CharacterFloatStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * CharacterFloatCacheTest - test for CharacterFloatTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CharacterFloatCacheTest {
    public void testCache() {
        CharacterFloatStorage cache = new CharacterFloatTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated('*');

        cache.save('*', 42f);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load('*'), 42f);

        cache.clear();

        assert !cache.isCalculated('*');
    }
}
