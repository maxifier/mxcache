package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ShortByteStorage;
import org.testng.annotations.Test;
import org.testng.Assert;

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
public class ShortByteCacheTest {
    public void testCache() {
        ShortByteStorage cache = new ShortByteTroveStorage();

        assert cache.size() == 0;

        assert !cache.isCalculated((short)42);

        cache.save((short)42, (byte)42);

        assert cache.size() == 1;
        Assert.assertEquals(cache.load((short)42), (byte)42);

        cache.clear();

        assert !cache.isCalculated((short)42);
    }
}
