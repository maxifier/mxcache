/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.regression;

import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

/**
 * MxCache51UTest
 *
 * see https://jira.maxifier.com/browse/MXCACHE-51
 *
 * @author alexander.kochurov@maxifier.com (Alexander Kochurov) (2013-07-02 11:40)
 */
@Test
public class MxCache51UTest {
    static abstract class X {
        public X() {
            test();
        }

        abstract int test();
    }

    static class Y extends X {
        @Cached
        int test() {
            return 3;
        }

    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void testRegression() {
        new Y();
    }
}
