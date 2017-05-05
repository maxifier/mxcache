/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

/**
 * BytecodeMatcherUTest
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({"MethodMayBeStatic"})
@Test
public class BytecodeMatcherUTest {
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testException() {
        new BytecodeMatcher("");
    }

    public void test() {
        assertFalse(testStrings("", "a"));
        assertTrue(testStrings("a", "a"));
        assertTrue(testStrings("bbba", "a"));
        assertTrue(testStrings("bab", "a"));
        assertTrue(testStrings("bbbabbbabbb", "a"));
        assertFalse(testStrings("cccccc", "a"));
        assertFalse(testStrings("ccccacc", "accc"));
        assertTrue(testStrings("ccccaccc", "accc"));
        assertTrue(testStrings("accc", "accc"));
        assertTrue(testStrings("acccccccc", "accc"));
        assertTrue(testStrings("bbbaccbbbacccccccc", "accc"));
    }

    private static boolean testStrings(String a, String b) {
        return new BytecodeMatcher(b).isContainedIn(a.getBytes());
    }

}
