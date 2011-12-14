package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.Cached;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.03.11
 * Time: 16:07
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
        Assert.assertEquals(t.zzz("1"), 0);
        Assert.assertEquals(t.zzz("2"), 1);
        Assert.assertEquals(t.zzz("1"), 0);
        Assert.assertEquals(t.zzz("2"), 1);
        Assert.assertEquals(t.zzz("3"), 2);

        Assert.assertEquals(t.zzz("4"), 3);
        Assert.assertEquals(t.zzz("5"), 4);
        Assert.assertEquals(t.zzz("6"), 5);

        // loaded from disk...
        Assert.assertEquals(t.zzz("1"), 0);
    }

    public void testSimpleOverflow() {
        X t = new X();
        Assert.assertEquals(t.xxx("1"), 0);
        Assert.assertEquals(t.xxx("2"), 1);
        Assert.assertEquals(t.xxx("1"), 0);
        Assert.assertEquals(t.xxx("3"), 2);

        Assert.assertEquals(t.xxx("4"), 3);
        Assert.assertEquals(t.xxx("5"), 4);
        Assert.assertEquals(t.xxx("6"), 5);

        Assert.assertEquals(t.xxx("1"), 6);
    }

    public void testEvictionPolicy() {
        Y t = new Y();
        Assert.assertEquals(t.yyy("1"), 0);
        Assert.assertEquals(t.yyy("1"), 0);
        Assert.assertEquals(t.yyy("1"), 0);
        Assert.assertEquals(t.yyy("2"), 1);
        Assert.assertEquals(t.yyy("2"), 1);

        // now "1" has frequency 3, "2" - 2.

        // this should evict value from cache
        Assert.assertEquals(t.yyy("3"), 2);

        // "1" should still be in cache
        Assert.assertEquals(t.yyy("1"), 0);

        // "3" is still in cache
        Assert.assertEquals(t.yyy("3"), 2);

        // "2" was evicted
        Assert.assertEquals(t.yyy("2"), 3);
    }
}
