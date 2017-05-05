/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.proxy.Resolvable;
import com.maxifier.mxcache.transform.*;
import org.mockito.Matchers;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({ "unchecked" })
@Test
public class ProxyingCacheGeneratorUTest {
    private static final int SIZE = 62780;
    private static final Statistics STATISTICS = mock(Statistics.class);

    @SuppressWarnings( { "UnusedDeclaration" })
    private static void ttt(@ReversibleTransform(
            forward = @Transform(owner = Transformator.class, method = "f"),
            backward = @Transform(owner = Transformator.class, method = "b")
    ) String x) {
    }

    public static class Transformator {
        public long f(String v) {
            return Long.parseLong(v);
        }

        public String b(long v) {
            return Long.toString(v);
        }
    }

    @Test
    public void testObject2Object() {
        ProxyFactory<String> factory = spy(new TestProxyFactory());
        TestCalculatable1 calculatable = spy(new TestCalculatable1());
        ObjectObjectCache<String, String> cache = (ObjectObjectCache)ProxyingCacheGenerator.wrapCacheWithProxy(ClassLoader.getSystemClassLoader(), calculatable, factory, String.class, String.class, ObjectObjectCache.class, new EmptyTransformGenerator(Object.class));
        verifyZeroInteractions(factory);
        verifyZeroInteractions(calculatable);
        assertEquals(cache.getOrCreate("123"), "__123");
        verify(factory).proxy(eq(String.class), Matchers.<Resolvable<String>>any());
        verify(calculatable).getOrCreate("123");

        assertEquals(cache.getSize(), SIZE);
        assertSame(cache.getStatistics(), STATISTICS);

        cache.invalidate();
        verify(calculatable).invalidate();
        verify(calculatable).getSize();
        verify(calculatable).getStatistics();
        verifyNoMoreInteractions(calculatable);
    }

    @Test
    public void testObject2ObjectTransform() throws NoSuchMethodException {
        Transformator t = spy(new Transformator());
        DefaultInstanceProvider.getInstance().bind(Transformator.class).toInstance(t);
        ProxyFactory<String> factory = spy(new TestProxyFactory());
        TestCalculatable1 calculatable = spy(new TestCalculatable1());
        TransformGenerator transform = TransformGeneratorFactoryImpl.getInstance().forMethod(ProxyingCacheGeneratorUTest.class.getDeclaredMethod("ttt", String.class));
        ObjectObjectCache<String, String> cache = (ObjectObjectCache)ProxyingCacheGenerator.wrapCacheWithProxy(ClassLoader.getSystemClassLoader(), calculatable, factory, String.class, String.class, ObjectObjectCache.class, transform);
        verifyZeroInteractions(factory);
        verifyZeroInteractions(calculatable);
        assertEquals(cache.getOrCreate("123"), "__123");
        verify(factory).proxy(eq(String.class), Matchers.<Resolvable<String>>any());
        verify(calculatable).getOrCreate("123");

        verify(t, atLeast(1)).f("123");
        verify(t, atLeast(1)).b(123L);
        verifyNoMoreInteractions(t);

        assertEquals(cache.getSize(), SIZE);
        assertSame(cache.getStatistics(), STATISTICS);

        cache.invalidate();
        verify(calculatable).invalidate();
        verify(calculatable).getSize();
        verify(calculatable).getStatistics();
        verifyNoMoreInteractions(calculatable);

        DefaultInstanceProvider.getInstance().clearBinding(Transformator.class);
    }

    @Test
    public void testPrimitive2Object() {
        ProxyFactory<String> factory = spy(new TestProxyFactory());
        TestCalculatable2 calculatable = spy(new TestCalculatable2());
        LongObjectCache<String> cache = (LongObjectCache)ProxyingCacheGenerator.wrapCacheWithProxy(ClassLoader.getSystemClassLoader(), calculatable, factory, long.class, String.class, LongObjectCache.class, null);
        verifyZeroInteractions(factory);
        verifyZeroInteractions(calculatable);
        assertEquals(cache.getOrCreate(123), "__123");
        verify(factory).proxy(eq(String.class), Matchers.<Resolvable<String>>any());
        verify(calculatable).getOrCreate(123);

        assertEquals(cache.getSize(), SIZE);
        assertSame(cache.getStatistics(), STATISTICS);

        verify(calculatable).getSize();
        verify(calculatable).getStatistics();
        cache.invalidate();
        verify(calculatable).invalidate();
        verifyNoMoreInteractions(calculatable);
    }

    @Test
    public void testNone2Object() {
        ProxyFactory<String> factory = spy(new TestProxyFactory());
        TestCalculatable3 calculatable = spy(new TestCalculatable3());
        ObjectCache<String> cache = (ObjectCache)ProxyingCacheGenerator.wrapCacheWithProxy(ClassLoader.getSystemClassLoader(), calculatable, factory, null, String.class, ObjectCache.class, null);
        verifyZeroInteractions(factory);
        verifyZeroInteractions(calculatable);
        assertEquals(cache.getOrCreate(), "__");
        verify(factory).proxy(eq(String.class), Matchers.<Resolvable<String>>any());
        verify(calculatable).getOrCreate();

        assertEquals(cache.getSize(), SIZE);
        assertSame(cache.getStatistics(), STATISTICS);

        verify(calculatable).getSize();
        verify(calculatable).getStatistics();
        cache.invalidate();
        verify(calculatable).invalidate();
        verifyNoMoreInteractions(calculatable);
    }

    private static class TestProxyFactory implements ProxyFactory<String> {
        @Override
        public String proxy(Class<String> expected, Resolvable<String> value) {
            return value.getValue();
        }
    }

    private static class TestCalculatable1 implements ObjectObjectCache<String, String> {
        @Override
        public String getOrCreate(String o) {
            return"__" + o;
        }

        @Override
        public void invalidate() {
        }

        @Override
        public int getSize() {
            return SIZE;
        }

        @Override
        public Statistics getStatistics() {
            return STATISTICS;
        }

        @Override
        public CacheDescriptor getDescriptor() {
            return null;
        }

        @Override
        public DependencyNode getDependencyNode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDependencyNode(DependencyNode node) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestCalculatable2 implements LongObjectCache<String> {
        @Override
        public String getOrCreate(long o) {
            return "__" + o;
        }

        @Override
        public void invalidate() {
        }

        @Override
        public int getSize() {
            return SIZE;
        }

        @Override
        public Statistics getStatistics() {
            return STATISTICS;
        }

        @Override
        public CacheDescriptor getDescriptor() {
            return null;
        }

        @Override
        public DependencyNode getDependencyNode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDependencyNode(DependencyNode node) {
            throw new UnsupportedOperationException();
        }
    }

    private static class TestCalculatable3 implements ObjectCache<String> {
        @Override
        public String getOrCreate() {
            return "__";
        }

        @Override
        public void invalidate() {
        }

        @Override
        public int getSize() {
            return SIZE;
        }

        @Override
        public Statistics getStatistics() {
            return STATISTICS;
        }

        @Override
        public CacheDescriptor getDescriptor() {
            return null;
        }

        @Override
        public DependencyNode getDependencyNode() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDependencyNode(DependencyNode node) {
            throw new UnsupportedOperationException();
        }
    }
}
