/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.CharacterDoubleStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * CharacterDoubleCacheTest - test for CharacterDoubleTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CharacterDoubleCacheTest {
    public void testCache() {
        CharacterDoubleStorage cache = new CharacterDoubleTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated('*');

        cache.save('*', 42d);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load('*'), 42d);

        cache.clear();

        assert !cache.isCalculated('*');
    }
}
