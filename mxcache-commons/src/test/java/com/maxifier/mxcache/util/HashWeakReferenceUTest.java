package com.maxifier.mxcache.util;

import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 12:21:14
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

        assert r1.get() != null;
        assert r2.get() != null;
        assert r3.get() != null;

        assert !r1.equals(r3);
        assert !r3.equals(r1);

        assert !r2.equals(r3);
        assert !r3.equals(r2);

        assert r1.equals(r2);
        assert r2.equals(r1);

        assert r1.equals(r1);
        assert r2.equals(r2);
        assert r3.equals(r3);

        assert r1.hashCode() == hash;
        assert r2.hashCode() == hash;
        assert r3.hashCode() == alive.hashCode();

        //noinspection UnusedAssignment,ReuseOfLocalVariable
        o = null;

        while (r1.get() != null) {
            System.gc();
            Thread.sleep(10);
        }

        assert r1.get() == null;
        assert r2.get() == null;

        // the object is alive!
        assert r3.get() != null;

        // still not equal
        assert !r1.equals(r3);
        assert !r3.equals(r1);

        assert !r2.equals(r3);
        assert !r3.equals(r2);

        // they are not equal now
        assert !r1.equals(r2);
        assert !r2.equals(r1);

        // but each reference is still equeal to itself
        assert r1.equals(r1);
        assert r2.equals(r2);
        assert r3.equals(r3);

        // and hash is still the same
        assert r1.hashCode() == hash;
        assert r2.hashCode() == hash;

        // we need to call hashCode as compiler may optimize code otherwise and remove reference to alive!
        assert r3.hashCode() == alive.hashCode();
    }
}
