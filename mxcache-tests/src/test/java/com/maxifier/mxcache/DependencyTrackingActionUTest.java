/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import static org.testng.Assert.assertEquals;

/**
 * DependencyTrackingActionUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-26 15:53)
 */
@Test
public class DependencyTrackingActionUTest {
    int v = 0;

    @Cached(tags = "xxx1")
    private String cache(String a) {
        return a + v;
    }

    @Cached(tags = "xxx2")
    private String cacheWAction(DependencyTrackingAction action) throws Exception {
        return action.trackDependencies(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return cache("test") + v;
            }
        });
    }

    public void testByCache() {
        final AtomicInteger clears = new AtomicInteger();
        DependencyTrackingAction action = new DependencyTrackingAction() {
            @Override
            protected void onClear() {
                clears.incrementAndGet();
            }
        };
        assertEquals(clears.get(), 0);
        action.trackDependencies(new Runnable() {
            @Override
            public void run() {
                cache("test");
            }
        });
        assertEquals(clears.get(), 0);
        CacheFactory.getCleaner().clearCacheByTag("xxx1");
        assertEquals(clears.get(), 1);
        CacheFactory.getCleaner().clearCacheByTag("xxx1");
        assertEquals(clears.get(), 2);
    }

    public void testTransparency() throws Exception {
        final AtomicInteger clears = new AtomicInteger();
        DependencyTrackingAction action = new DependencyTrackingAction() {
            @Override
            protected void onClear() {
                clears.incrementAndGet();
            }
        };
        v = 0;
        assertEquals(cacheWAction(action), "test00");
        v = 1;
        assertEquals(cacheWAction(action), "test00");

        assertEquals(clears.get(), 0);
        CacheFactory.getCleaner().clearCacheByTag("xxx1");
        assertEquals(clears.get(), 1);

        assertEquals(cacheWAction(action), "test11");
    }

    public void testResource() throws Exception {
        final AtomicInteger clears = new AtomicInteger();
        final MxResource r = MxResourceFactory.getResource("test");
        final DependencyTrackingAction action = new DependencyTrackingAction() {
            @Override
            protected void onClear() {
                clears.incrementAndGet();
            }
        };
        r.clearDependentCaches();
        assertEquals(clears.get(), 0);
        action.trackDependencies(new Runnable() {
            @Override
            public void run() {
                r.readStart();
                r.readEnd();
            }
        });
        assertEquals(clears.get(), 0);
        r.clearDependentCaches();
        assertEquals(clears.get(), 1);
        r.clearDependentCaches();
        assertEquals(clears.get(), 2);

        final AtomicInteger anotherClears = new AtomicInteger();

        v = 0;
        DependencyTrackingAction anotherAction = new DependencyTrackingAction() {
            @Override
            protected void onClear() {
                anotherClears.incrementAndGet();
            }
        };
        assertEquals(cacheWAction(anotherAction), "test00");
        v = 1;
        assertEquals(cacheWAction(anotherAction), "test00");

        r.clearDependentCaches();
        assertEquals(clears.get(), 3);

        assertEquals(cacheWAction(anotherAction), "test00");

        anotherAction.trackDependencies(new Runnable() {
            @Override
            public void run() {
                action.mark();
            }
        });

        assertEquals(anotherClears.get(), 0);
        r.clearDependentCaches();
        assertEquals(clears.get(), 4);
        assertEquals(anotherClears.get(), 1);
        // cache(String) is not cleared as it doesn't depend on resource
        assertEquals(cacheWAction(anotherAction), "test01");
    }
}
