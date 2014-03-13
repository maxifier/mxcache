/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;
import org.testng.annotations.Test;

/**
 * ShortCacheTest - test for ShortStorageImpl
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ShortCacheTest {
    public void testCache() {
        ShortStorage storage = new ShortStorageImpl();
        assert !storage.isCalculated();
        assert storage.size() == 0;
        storage.save((short)42);
        assert storage.load() == (short)42;
        assert storage.isCalculated();
        assert storage.size() == 1;
        storage.clear();
        assert storage.size() == 0;
        assert !storage.isCalculated();
    }
}
