package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import org.testng.annotations.Test;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
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
