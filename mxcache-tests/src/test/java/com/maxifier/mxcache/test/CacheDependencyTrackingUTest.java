package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.resource.TrackDependency;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 17.06.11
 * Time: 13:49
 */
@Test
public class CacheDependencyTrackingUTest {
    static class X {
        int x;

        @Cached(tags = "x_dep", group = "x_group")
        @TrackDependency(DependencyTracking.INSTANCE)
        private int a() {
            return x;
        }
    }

    X x = new X();

    @Cached
    @TrackDependency(DependencyTracking.INSTANCE)
    private int b() {
        return x.a();
    }

    @BeforeMethod
    private void reset() {
        x.x = 0;
        CacheFactory.getCleaner().clearCacheByInstances(x, this);
    }

    public void testClearByTag() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByTag("x_dep");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByGroup() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByGroup("x_group");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearInstanceByTag() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearInstanceByTag(x, "x_dep");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearInstanceByGroup() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearInstanceByGroup(x, "x_group");

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByInstance() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByInstance(x);

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }

    public void testClearByClass() {
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        x.x = 2;
        Assert.assertEquals(x.a(), 0);
        Assert.assertEquals(b(), 0);

        CacheFactory.getCleaner().clearCacheByClass(X.class);

        Assert.assertEquals(x.a(), 2);
        Assert.assertEquals(b(), 2);
    }
}
