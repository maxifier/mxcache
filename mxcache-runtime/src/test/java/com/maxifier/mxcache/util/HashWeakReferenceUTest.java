/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import org.testng.Assert;
import org.testng.annotations.Test;

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
        Assert.assertFalse(r1.equals("123"));
        //noinspection NullableProblems,ObjectEqualsNull
        Assert.assertFalse(r1.equals(null));

        Assert.assertNotNull(r1.get());
        Assert.assertNotNull(r2.get());
        Assert.assertNotNull(r3.get());

        Assert.assertFalse(r1.equals(r3));
        Assert.assertFalse(r3.equals(r1));

        Assert.assertFalse(r2.equals(r3));
        Assert.assertFalse(r3.equals(r2));

        Assert.assertEquals(r1, r2);
        Assert.assertEquals(r2, r1);

        Assert.assertEquals(r1, r1);
        Assert.assertEquals(r2, r2);
        Assert.assertEquals(r3, r3);

        Assert.assertEquals(r1.hashCode(), hash);
        Assert.assertEquals(r2.hashCode(), hash);
        Assert.assertEquals(r3.hashCode(), alive.hashCode());

        //noinspection UnusedAssignment,ReuseOfLocalVariable
        o = null;

        while (r1.get() != null) {
            System.gc();
            Thread.sleep(10);
        }

        Assert.assertNull(r1.get());
        Assert.assertNull(r2.get());

        // the object is alive!
        Assert.assertNotNull(r3.get());

        // still not equal
        Assert.assertFalse(r1.equals(r3));
        Assert.assertFalse(r3.equals(r1));

        Assert.assertFalse(r2.equals(r3));
        Assert.assertFalse(r3.equals(r2));

        // they are not equal now
        Assert.assertFalse(r1.equals(r2));
        Assert.assertFalse(r2.equals(r1));

        // but each reference is still equeal to itself
        Assert.assertEquals(r1, r1);
        Assert.assertEquals(r2, r2);
        Assert.assertEquals(r3, r3);

        // and hash is still the same
        Assert.assertEquals(r1.hashCode(), hash);
        Assert.assertEquals(r2.hashCode(), hash);

        // we need to call hashCode as compiler may optimize code otherwise and remove reference to alive!
        Assert.assertEquals(r3.hashCode(), alive.hashCode());
    }
}
