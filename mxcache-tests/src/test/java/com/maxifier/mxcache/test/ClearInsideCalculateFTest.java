package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.10.11
 * Time: 21:15
 */
@Test
public class ClearInsideCalculateFTest {
    private static final MxResource RESOURCE = MxResourceFactory.getResource("test");
    int x = 11;
    int y = 3;

    @Cached
    private int x() {
        CacheFactory.getCleaner().clearCacheByInstance(this);
        return x++;
    }

    @Cached
    private int y() {
        CacheFactory.getCleaner().clearCacheByInstance(this);
        return y++;
    }

    public void testClearInCalculate() {
        assertEquals(x(), 11);
        assertEquals(x(), 11);

        assertEquals(y(), 3);
        assertEquals(y(), 3);

        assertEquals(x(), 12);
        assertEquals(x(), 12);

        assertEquals(y(), 4);
        assertEquals(y(), 4);
    }

    int r = 6;
    int s = 17;

    @Cached
    private int r() {
        RESOURCE.readStart();
        RESOURCE.readEnd();

        RESOURCE.clearDependentCaches();

        return r++;
    }

    @Cached
    private int s() {
        RESOURCE.readStart();
        RESOURCE.readEnd();

        RESOURCE.clearDependentCaches();

        return s++;
    }

    public void testThroughResource() {
        assertEquals(r(), 6);
        assertEquals(r(), 6);

        assertEquals(s(), 17);
        assertEquals(s(), 17);

        assertEquals(r(), 7);
        assertEquals(r(), 7);

        assertEquals(s(), 18);
        assertEquals(s(), 18);
    }
}
