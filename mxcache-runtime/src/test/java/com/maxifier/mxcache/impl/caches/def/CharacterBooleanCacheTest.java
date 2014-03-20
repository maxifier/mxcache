/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.CharacterBooleanStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

/**
 * CharacterBooleanCacheTest - test for CharacterBooleanTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CharacterBooleanCacheTest {
    public void testCache() {
        CharacterBooleanStorage cache = new CharacterBooleanTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated('*');

        cache.save('*', true);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load('*'), true);

        cache.clear();

        assert !cache.isCalculated('*');
    }
}
