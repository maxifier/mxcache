package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.ByteByteStorage;
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
