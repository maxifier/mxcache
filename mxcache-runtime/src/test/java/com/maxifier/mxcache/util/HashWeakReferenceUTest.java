/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import org.testng.annotations.Test;

import static junit.framework.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class HashWeakReferenceUTest {

    public void testEquality() throws Exception {
        Object o = new Object();
        int hash = o.hashCode();

        Object alive = new Object();

        HashWeakReference<Object> r1 = new HashWeakReference<Object>(o);
        HashWeakReference<Object> r2 = new HashWeakReference<Object>(o);
        HashWeakReference<Object> r3 = new HashWeakReference<Object>(alive);

        //noinspection EqualsBetweenInconvertibleTypes
        assertFalse(r1.equals("123"));
        //noinspection NullableProblems,ObjectEqualsNull
        assertFalse(r1.equals(null));

        assertNotNull(r1.get());
        assertNotNull(r2.get());
        assertNotNull(r3.get());

        assertFalse(r1.equals(r3));
        assertFalse(r3.equals(r1));

        assertFalse(r2.equals(r3));
        assertFalse(r3.equals(r2));

        assertEquals(r1, r2);
        assertEquals(r2, r1);

        assertEquals(r1, r1);
        assertEquals(r2, r2);
        assertEquals(r3, r3);

        assertEquals(r1.hashCode(), hash);
        assertEquals(r2.hashCode(), hash);
        assertEquals(r3.hashCode(), alive.hashCode());

        //noinspection UnusedAssignment,ReuseOfLocalVariable
        o = null;

        while (r1.get() != null) {
            System.gc();
            Thread.sleep(10);
        }

        assertNull(r1.get());
        assertNull(r2.get());

        // the object is alive!
        assertNotNull(r3.get());

        // still not equal
        assertFalse(r1.equals(r3));
        assertFalse(r3.equals(r1));

        assertFalse(r2.equals(r3));
        assertFalse(r3.equals(r2));

        // they are not equal now
        assertFalse(r1.equals(r2));
        assertFalse(r2.equals(r1));

        // but each reference is still equeal to itself
        assertEquals(r1, r1);
        assertEquals(r2, r2);
        assertEquals(r3, r3);

        // and hash is still the same
        assertEquals(r1.hashCode(), hash);
        assertEquals(r2.hashCode(), hash);

        // we need to call hashCode as compiler may optimize code otherwise and remove reference to alive!
        assertEquals(r3.hashCode(), alive.hashCode());
    }
}
