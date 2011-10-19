package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.storage.*;
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
public class DoubleCacheTest {
    public void testCache() {
        DoubleStorage storage = new DoubleStorageImpl();
        assert !storage.isCalculated();
        assert storage.size() == 0;
        storage.save(42d);
        assert storage.load() == 42d;
        assert storage.isCalculated();
        assert storage.size() == 1;
        storage.clear();
        assert storage.size() == 0;
        assert !storage.isCalculated();
    }
}
