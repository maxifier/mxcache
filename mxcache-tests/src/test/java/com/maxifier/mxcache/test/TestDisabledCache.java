/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.Cached;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class TestDisabledCache {
    private static int value1;
    private static int value2;

    @Cached
    public static int get1() {
        return value1++;
    }

    @Cached(tags = "disabled_cache")
    public static int get2() {
        return value2++;
    }

    @Test
    public void test() {
        value1 = 7;
        value2 = 7;
        Assert.assertEquals(get1(), 7);
        Assert.assertEquals(get1(), 7);
        Assert.assertEquals(get1(), 7);
        Assert.assertEquals(get2(), 7);
        Assert.assertEquals(get2(), 8);
        Assert.assertEquals(get2(), 9);
    }
}
