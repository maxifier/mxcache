/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class OverloadTest {
    @Cached
    private String x(String x) {
        return x;
    }

    @Cached
    private String x(Integer x) {
        return x.toString();
    }

    public void test() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        assert x(3).equals("3");
        assert x("3").equals("3");
    }
}
