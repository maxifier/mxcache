/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import org.testng.annotations.Test;

/**
 * ObjectCacheTest
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ObjectCacheTest {
    public void testCache() {
        ObjectStorage<String> cache = new ObjectStorageImpl<String>();
        assert cache.load() == Storage.UNDEFINED;
        assert cache.size() == 0;
        cache.save("123");
        assert cache.load().equals("123");
        assert cache.size() == 1;
        cache.clear();
        assert cache.size() == 0;
        assert cache.load() == Storage.UNDEFINED;
    }
}
