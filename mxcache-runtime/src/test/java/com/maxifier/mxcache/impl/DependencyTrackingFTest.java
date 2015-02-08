/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTestHelper;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.impl.resource.nodes.MultipleDependencyNode;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import com.maxifier.mxcache.resource.ResourceDependency;
import com.maxifier.mxcache.resource.TrackDependency;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.resource.MxResource;
import org.testng.annotations.Test;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.DependencyTracking;

import java.util.Set;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 * 
 */
@Test
public class DependencyTrackingFTest {

    public static class MyClass {
        final IntIntCache cache;

        public MyClass(CacheManager<MyClass> d) {
            // static cache don't want instance!
            cache = spy((IntIntCache) d.createCache(d.getDescriptor().isStatic() ? null : this));
        }

        @Cached
        @ResourceDependency("DependencyTrackingFTest.manualTracking")
        @TrackDependency(DependencyTracking.NONE)
        public int manualTracking(int x) {
            return x;
        }

        @TrackDependency(DependencyTracking.STATIC)
        public static int staticMethodWithStaticTracking(int x) {
            return x;
        }

        @TrackDependency (DependencyTracking.STATIC)
        public int instanceMethodWithStaticTracking(int x) {
            return x;
        }

        @TrackDependency(DependencyTracking.INSTANCE)
        public int instanceMethodWithInstanceTracking(int x) {
            return x;
        }
    }

    public void testManualTracking() {
        // мы используем уникальное имя ресурса, чтобы никто другой не мог использовать его
        MxResource r = MxResourceFactory.getResource("DependencyTrackingFTest.manualTracking");

        CacheDescriptor<MyClass> d = new CacheDescriptor<MyClass>(MyClass.class, 0, int.class, int.class, new MyIntIntCalculatable(r), "manualTracking", "(I)I", null, null, null, null);
        CacheManager<MyClass> descriptor = createDefaultManager(d);
        MyClass t = new MyClass(descriptor);

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(r);
        assertEquals(nodes.size(), 1);
        DependencyNode node = nodes.iterator().next();
        assertTrue(node instanceof MultipleDependencyNode);
        node.invalidate();
        verify(t.cache).invalidate();
    }

    private CacheManager<MyClass> createDefaultManager(CacheDescriptor<MyClass> d) {
        return DefaultStrategy.getInstance().getManager(CacheFactory.getDefaultContext(), d);
    }

    public void testStaticMethodWithStaticTracking() {
        // мы используем уникальное имя ресурса, чтобы никто другой не мог использовать его
        MxResource r = MxResourceFactory.getResource("DependencyTrackingFTest#staticMethodWithStaticTracking");

        CacheDescriptor<MyClass> d = new CacheDescriptor<MyClass>(MyClass.class, 0, int.class, int.class, new MyIntIntCalculatable(r), "staticMethodWithStaticTracking", "(I)I", null, null, null, null);
        CacheManager<MyClass> descriptor = createDefaultManager(d);
        MyClass t = new MyClass(descriptor);
        assert !DependencyTracker.hasUnderlyingNode();
        int v = t.cache.getOrCreate(3);
        assert !DependencyTracker.hasUnderlyingNode();
        assert v == 3;

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(r);
        assert nodes.size() == 1;
        DependencyNode node = nodes.iterator().next();
        assert node instanceof SingletonDependencyNode;

        node.invalidate();
        verify(t.cache).invalidate();
    }

    public void testInstanceMethodWithStaticTracking() {
        // мы используем уникальное имя ресурса, чтобы никто другой не мог использовать его
        MxResource r = MxResourceFactory.getResource("DependencyTrackingFTest#testInstanceMethodWithStaticTracking");

        CacheDescriptor<MyClass> d = new CacheDescriptor<MyClass>(MyClass.class, 1, int.class, int.class, new MyIntIntCalculatable(r), "instanceMethodWithStaticTracking", "(I)I", null, null, null, null);
        CacheManager<MyClass> descriptor = createDefaultManager(d);
        MyClass t = new MyClass(descriptor);
        assert !DependencyTracker.hasUnderlyingNode();
        int v = t.cache.getOrCreate(3);
        assert !DependencyTracker.hasUnderlyingNode();
        assert v == 3;

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(r);
        assert nodes.size() == 1;
        DependencyNode node = nodes.iterator().next();
        assert node instanceof MultipleDependencyNode;
        node.invalidate();
        verify(t.cache).invalidate();
    }

    public void testInstanceMethodWithInstanceTracking() {
        // мы используем уникальное имя ресурса, чтобы никто другой не мог использовать его
        MxResource r = MxResourceFactory.getResource("DependencyTrackingFTest#testInstanceMethodWithInstanceTracking");

        CacheDescriptor<MyClass> d = new CacheDescriptor<MyClass>(MyClass.class, 2, int.class, int.class, new MyIntIntCalculatable(r), "instanceMethodWithInstanceTracking", "(I)I", null, null, null, null);
        CacheManager<MyClass> descriptor = createDefaultManager(d);
        MyClass t = new MyClass(descriptor);
        assert !DependencyTracker.hasUnderlyingNode();
        int v = t.cache.getOrCreate(3);
        assert !DependencyTracker.hasUnderlyingNode();
        assert v == 3;

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(r);
        assert nodes.size() == 1;
        DependencyNode node = nodes.iterator().next();
        assert node instanceof SingletonDependencyNode;
        node.invalidate();
        verify(t.cache).invalidate();
    }

    private static class MyIntIntCalculatable implements IntIntCalculatable {
        private final MxResource resource;

        MyIntIntCalculatable(MxResource resource) {
            this.resource = resource;
        }

        @Override
        public int calculate(Object owner, int o) {
            assert DependencyTracker.hasUnderlyingNode();
            resource.readStart();
            try {
                return o;
            } finally {
                resource.readEnd();
            }
        }
    }
}
