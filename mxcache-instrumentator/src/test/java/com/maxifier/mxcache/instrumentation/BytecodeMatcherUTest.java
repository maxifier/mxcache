package com.maxifier.mxcache.instrumentation;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

import org.testng.annotations.Test;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 27.01.2010
 * Time: 10:13:45
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
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
