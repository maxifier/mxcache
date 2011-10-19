package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.EvictionPolicyEnum;
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
    class T {
        int i;

        @Cached(name = "xxx")
        @UseEhcache(maxElements = 3)
        private int xxx(String s) {
            return i++;
        }

        @Cached(name = "yyy")
        @UseEhcache(maxElements = 2, memoryEvictionPolicy = EvictionPolicyEnum.LFU)
        private int yyy(String s) {
            return i++;
        }
    }

    public void testSimpleOverflow() {
        T t = new T();
        Assert.assertEquals(t.xxx("1"), 0);
        Assert.assertEquals(t.xxx("2"), 1);
        Assert.assertEquals(t.xxx("1"), 0);
        Assert.assertEquals(t.xxx("3"), 2);

        Assert.assertEquals(t.xxx("4"), 3);
        Assert.assertEquals(t.xxx("5"), 4);
        Assert.assertEquals(t.xxx("6"), 5);

        Assert.assertEquals(t.xxx("1"), 6);
    }

    public void testDifferentInstances() {
        T t1 = new T();
        T t2 = new T();
        Assert.assertEquals(t1.xxx("1"), 0);
        Assert.assertEquals(t2.xxx("1"), 0);
        Assert.assertEquals(t1.xxx("2"), 1);
        Assert.assertEquals(t2.xxx("2"), 1);
        Assert.assertEquals(t1.xxx("3"), 2);
        Assert.assertEquals(t2.xxx("4"), 2);
    }

    public void testEvictionPolicy() {
        T t = new T();
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
