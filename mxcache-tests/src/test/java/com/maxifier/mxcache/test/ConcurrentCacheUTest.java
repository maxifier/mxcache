/*
 * Copyright (c) 2008-2015 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.concurrent.ConcurrentCache;
import org.testng.annotations.Test;

import javax.annotation.Nullable;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * ConcurrentCacheUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2015-02-02 11:01)
 */
@Test
public class ConcurrentCacheUTest {
    int i = 0;

    @Cached
    @ConcurrentCache
    public String testCache(@Nullable String s) {
        if (s == null) {
            return "test";
        }
        if (s.equals("test")) {
            return null;
        }
        return s + i;
    }

    public void simpleTest() {
        i = 0;
        assertEquals(testCache("1"), "10");
        i++;
        assertEquals(testCache("2"), "21");
        assertEquals(testCache("1"), "10");

        assertEquals(testCache(null), "test");
        assertNull(testCache("test"));
    }
}