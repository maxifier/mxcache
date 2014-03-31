/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * EhcacheStorageUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class EhcacheStorageUTest {

    @SuppressWarnings( { "UnusedParameters" })
    class X {
        int i;

        @Cached(name = "xxx")
        @UseEhcache
        private int xxx(String s) {
            return i++;
        }

        @Cached(name = "xxx")
        @UseEhcache(configURL = "classpath://META-INF/ehcache2.xml")
        private int xxx2(String s) {
            return i++;
        }
    }

    class Y {
        int i;

        @Cached(name = "yyy")
        @UseEhcache
        private int yyy(String s) {
            return i++;
        }
    }

    class Z {
        int i;

        @Cached(name = "zzz")
        @UseEhcache
        private int zzz(String s) {
            return i++;
        }
    }

    public void testDiskStore() {
        Z t = new Z();
        assertEquals(t.zzz("1"), 0);
        assertEquals(t.zzz("2"), 1);
        assertEquals(t.zzz("1"), 0);
        assertEquals(t.zzz("2"), 1);
        assertEquals(t.zzz("3"), 2);

        assertEquals(t.zzz("4"), 3);
        assertEquals(t.zzz("5"), 4);
        assertEquals(t.zzz("6"), 5);

        // loaded from disk...
        assertEquals(t.zzz("1"), 0);
    }

    public void testSimpleOverflow() {
        X t = new X();
        assertEquals(t.xxx("1"), 0);
        assertEquals(t.xxx("2"), 1);
        assertEquals(t.xxx("1"), 0);
        assertEquals(t.xxx("3"), 2);

        assertEquals(t.xxx("4"), 3);
        assertEquals(t.xxx("5"), 4);
        assertEquals(t.xxx("6"), 5);

        assertEquals(t.xxx("1"), 6);
    }

    public void testCustomConfiguration() {
        X t = new X();
        assertEquals(t.xxx2("1"), 0);
        assertEquals(t.xxx2("2"), 1);
        assertEquals(t.xxx2("1"), 0);
        assertEquals(t.xxx2("3"), 2);

        assertEquals(t.xxx2("2"), 3);
    }

    public void testEvictionPolicy() {
        Y t = new Y();
        assertEquals(t.yyy("1"), 0);
        assertEquals(t.yyy("1"), 0);
        assertEquals(t.yyy("1"), 0);
        assertEquals(t.yyy("2"), 1);
        assertEquals(t.yyy("2"), 1);

        // now "1" has frequency 3, "2" - 2.

        // this should evict value from cache
        assertEquals(t.yyy("3"), 2);

        // "1" should still be in cache
        assertEquals(t.yyy("1"), 0);

        // "3" is still in cache
        assertEquals(t.yyy("3"), 2);

        // "2" was evicted
        assertEquals(t.yyy("2"), 3);
    }
}
