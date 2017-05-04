/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.regression;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.hashing.IdentityHashing;
import com.maxifier.mxcache.transform.WeakKey;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Trove3BugTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-06-03 10:07)
 */
public class TupleObjectWeakStorageWithHashingStrategyUTest {
    @Cached
    private Object cachedTuple(@WeakKey @IdentityHashing String s, int x) {
        return System.identityHashCode(s) + x;
    }

    @Test
    public void test() {
        //noinspection AssertEqualsBetweenInconvertibleTypesTestNG
        assertEquals(cachedTuple("1", 3), System.identityHashCode("1") + 3);
        @SuppressWarnings("RedundantStringConstructorCall")
        String s = new String("1");
        //noinspection AssertEqualsBetweenInconvertibleTypesTestNG
        assertEquals(cachedTuple(s, 3), System.identityHashCode(s) + 3);
    }
}
