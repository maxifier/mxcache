/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.CharacterShortStorage;
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
public class CharacterShortCacheTest {
    public void testCache() {
        CharacterShortStorage cache = new CharacterShortTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated('*');

        cache.save('*', (short)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load('*'), (short)42);

        cache.clear();

        assert !cache.isCalculated('*');
    }
}
