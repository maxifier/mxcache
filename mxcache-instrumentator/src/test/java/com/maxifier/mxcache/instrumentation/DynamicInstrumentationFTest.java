/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.clean.CacheCleaner;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.CacheContextImpl;
import com.maxifier.mxcache.impl.CacheId;
import com.maxifier.mxcache.impl.DefaultStrategy;
import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CacheProvider;
import com.maxifier.mxcache.provider.CacheProviderInterceptor;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.Callable;

import static com.maxifier.mxcache.asm.Opcodes.ACC_PUBLIC;
import static com.maxifier.mxcache.asm.Opcodes.IADD;
import static com.maxifier.mxcache.asm.Type.INT_TYPE;
import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * DynamicInstrumentationFTest
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings("ParameterCanBeLocal")
@Test
public class DynamicInstrumentationFTest {
    private static final Object[] V2228 = { InstrumentatorProvider.getAvailableVersions().get("2.2.28"), null };
    private static final Object[] V262  = { InstrumentatorProvider.getAvailableVersions().get("2.6.2"), null };

    @DataProvider(name = "v2228")
    public Object[][] v2228() {
        return new Object[][] { V2228 };
    }

    @DataProvider(name = "v262")
    public Object[][] v262() {
        return new Object[][] { V262 };
    }

    @DataProvider(name = "all")
    public Object[][] all() {
        return new Object[][] { V2228, V262 };
    }

    /**
     * Делает хитрый финт ушами для запуска теста из-под мавена.
     * Т.к. при запуске теста из-под мавена не удается корректно запустить динамическую инструментацию,
     * она пускается вручную.
     * Для этого грузится байт-код класса, инструментируется, и грузится отдельным classLoader-ом.
     * Конфликта не возникает, т.к. этот класс и исходный (не инструментированный) загружены разными класс-лоадерами,
     * хоть и имеют одинаковое имя.
     *
     * @param instrumentator instrumentator
     * @param cl class loader
     * @return экземпляр
     * @throws Exception если что-то не так
     */
    private TestCached loadCached(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        return (TestCached) instrumentClass(TestCachedImpl.class, instrumentator, cl).newInstance();
    }

    private TestProxied loadProxied(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        return (TestProxied) instrumentClass(TestProxiedImpl.class, instrumentator, cl).newInstance();
    }

    private Point loadPoint(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        return (Point) instrumentClass(PointImpl.class, instrumentator, cl).newInstance();
    }

    @SuppressWarnings("AssertEqualsBetweenInconvertibleTypesTestNG")
    @Test(dataProvider = "all")
    public void testAnotherClassLoader(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        cl = new ClassLoader() {};

        ClassGenerator g1 = new ClassGenerator(ACC_PUBLIC, "$test1", Object.class);
        g1.defineDefaultConstructor();
        Class<Object> g1c = CodegenHelper.loadClass(cl, g1.toByteArray());

        ClassGenerator g2 = new ClassGenerator(ACC_PUBLIC, "$test2", Object.class);
        g2.defineField(ACC_PUBLIC, "i", INT_TYPE);
        g2.defineDefaultConstructor();
        MxGeneratorAdapter m = g2.defineMethod(ACC_PUBLIC, "x", INT_TYPE, g1.getThisType());

        AnnotationVisitor v = m.visitAnnotation("Lcom/maxifier/mxcache/Cached;", true);
        v.visitArray("tags");
        v.visit("group", "");
        v.visit("name", "");
        v.visit("activity", "");

        m.start();
        int i = m.newLocal(INT_TYPE);
        m.loadThis();
        m.getField(g2.getThisType(), "i", INT_TYPE);
        m.storeLocal(i);
        m.loadThis();
        m.push(1);
        m.loadLocal(i);
        m.visitInsn(IADD);
        m.putField(g2.getThisType(), "i", INT_TYPE);

        m.loadLocal(i);
        m.returnValue();
        m.endMethod();

        Class g2c = instrumentAndLoad(instrumentator, cl, g2.toByteArray());

        Object o = g2c.newInstance();

        //noinspection unchecked
        Method r = g2c.getDeclaredMethod("x", g1c);

        Object v1 = g1c.newInstance();
        Object v2 = g1c.newInstance();
        Object v3 = g1c.newInstance();

        assertEquals(r.invoke(o, v1), 0);
        assertEquals(r.invoke(o, v2), 1);
        assertEquals(r.invoke(o, v3), 2);
        assertEquals(r.invoke(o, v1), 0);
        assertEquals(r.invoke(o, v1), 0);
        assertEquals(r.invoke(o, v2), 1);
        assertEquals(r.invoke(o, v2), 1);
        assertEquals(r.invoke(o, v3), 2);
        assertEquals(r.invoke(o, v3), 2);
    }

    @Test(dataProvider = "all")
    public void testSerialVersionUID(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(t);
        oos.close();
        ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream oin = new ObjectInputStream(bin);
        Assert.assertTrue(oin.readObject() instanceof TestCachedImpl);
    }

    @Test(dataProvider = "all", expectedExceptions = IllegalCachedClass.class)
    public void testCachedInterface(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        instrumentClass(CachedInterface.class, instrumentator, cl);
    }

    @Test (dataProvider = "all", expectedExceptions = IllegalCachedClass.class)
    public void testCachedAbstractMethod(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        instrumentClass(CachedAbstract.class, instrumentator, cl);
    }

    @Test (dataProvider = "all", expectedExceptions = IllegalCachedClass.class)
    public void testCachedVoidMethod(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        instrumentClass(CachedVoid.class, instrumentator, cl);
    }

    @Test (dataProvider = "all", expectedExceptions = IllegalCachedClass.class)
    public void testCachedNativeMethod(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        instrumentClass(CachedNative.class, instrumentator, cl);
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    @Test(dataProvider = "all")
    public void testNonCached(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        // тут сработает отсечение на дескриптор
        assert instrumentator.instrument(CodegenHelper.getByteCode(NonCachedShortcut.class)) == null;
        // а тут сработает отсечение на отсутствие кэшированных методов
        assert instrumentator.instrument(CodegenHelper.getByteCode(NotCached.class)) == null;
    }

    @Test(dataProvider = "all")
    public void testTooManyCachesCreated(Instrumentator instrumentator, ClassLoader cl) throws Exception {

        SpyCacheProvider spy = new SpyCacheProvider(CacheFactory.getProvider());

        CacheFactory.setProviderOverride(spy);
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        c.newInstance();

        int cachesCreated = 0;
        for (Object[] objects : spy.getQueries()) {
            if (objects[0].equals("createCache")) {
                cachesCreated++;
            }
        }
        int cachedMethods = 0;
        for (Method method : TestCachedImpl.class.getDeclaredMethods()) {
            if (method.getAnnotation(Cached.class) != null) {
                cachedMethods++;
            }
        }
        Assert.assertEquals(cachesCreated, cachedMethods);
    }

    @Test(dataProvider = "v2228")
    public void testCustomContext(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        TestCached defInstance = (TestCached) c.newInstance();
        Assert.assertEquals(defInstance.nullCache("test"), "test0");
        Assert.assertEquals(defInstance.nullCache("test"), "test1");
        Assert.assertEquals(defInstance.nullCache("test"), "test2");

        Assert.assertEquals(defInstance.nullCache("past"), "past3");
        Assert.assertEquals(defInstance.nullCache("past"), "past4");
        Assert.assertEquals(defInstance.nullCache("past"), "past5");

        TestCached cusInstance = (TestCached) c.getConstructor(CacheContext.class).newInstance(new CacheContextImpl(new OverrideInstanceProvider()));

        Assert.assertEquals(cusInstance.nullCache("test"), "test0");
        Assert.assertEquals(cusInstance.nullCache("test"), "test0");
        Assert.assertEquals(cusInstance.nullCache("test"), "test0");

        Assert.assertEquals(cusInstance.nullCache("past"), "past1");
        Assert.assertEquals(cusInstance.nullCache("past"), "past1");
        Assert.assertEquals(cusInstance.nullCache("past"), "past1");
    }

    @Test(dataProvider = "v2228", expectedExceptions = IllegalCachedClass.class)
    public void testReadBoundResourceFromStaticMethod(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        instrumentClass(StaticMethodAccessedBoundResource.class, instrumentator, cl);
    }

    @Test(dataProvider = "all")
    public void testResourceWithException(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        TestCached o = (TestCached) c.newInstance();
        try {
            o.readResourceWithException(new Runnable() {
                @Override
                public void run() {
                    assertTrue(MxResourceFactory.getResource("test").isReading());
                }
            });
            fail("Should throw an exception");
        } catch (IllegalStateException e) {
            // that's ok
        }
        assertFalse(MxResourceFactory.getResource("test").isReading());
    }

    @Test(dataProvider = "v2228")
    public void testBoundResourceRead(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        TestCached o1 = (TestCached) c.newInstance();
        TestCached o2 = (TestCached) c.newInstance();


        CleaningNode n1 = mock(CleaningNode.class);
        CleaningNode n2 = mock(CleaningNode.class);

        DependencyNode dn1 = new SingletonDependencyNode(n1);
        DependencyNode dn2 = new SingletonDependencyNode(n2);

        DependencyNode p = DependencyTracker.track(dn1);
        o1.readResource();

        o1.readStatic();
        o2.readStatic();

        DependencyTracker.exit(p);

        p = DependencyTracker.track(dn2);
        o2.readResource();

        o1.readStatic();
        o2.readStatic();

        DependencyTracker.exit(p);

        o1.writeResource();

        verify(n1).invalidate();

        verifyZeroInteractions(n2);

        o2.writeResource();
        verifyZeroInteractions(n1);

        verify(n2).invalidate();

        o1.writeStatic();

        verify(n2, times(2)).invalidate();
        verify(n1, times(2)).invalidate();

        o2.writeStatic();

        verify(n2, times(3)).invalidate();
        verify(n1, times(3)).invalidate();
    }

    @Test(dataProvider = "v2228")
    public void testBoundResourceSerialization(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        TestCached o1 = (TestCached) c.newInstance();

        TestCached o2 = serialize(o1);

        CleaningNode n1 = mock(CleaningNode.class);
        CleaningNode n2 = mock(CleaningNode.class);

        DependencyNode dn1 = new SingletonDependencyNode(n1);
        DependencyNode dn2 = new SingletonDependencyNode(n2);

        DependencyNode p = DependencyTracker.track(dn1);
        o1.readResource();

        o1.readStatic();
        o2.readStatic();

        DependencyTracker.exit(p);

        p = DependencyTracker.track(dn2);
        o2.readResource();

        o1.readStatic();
        o2.readStatic();

        DependencyTracker.exit(p);

        o1.writeResource();

        verify(n1).invalidate();

        verifyZeroInteractions(n2);

        o2.writeResource();
        verifyZeroInteractions(n1);

        verify(n2).invalidate();

        o1.writeStatic();

        verify(n2, times(2)).invalidate();
        verify(n1, times(2)).invalidate();

        o2.writeStatic();

        verify(n2, times(3)).invalidate();
        verify(n1, times(3)).invalidate();
    }

    @Test(dataProvider = "v2228")
    public void testMarkerAnnotations(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestProxiedImpl.class, instrumentator, cl);
        Assert.assertEquals(new Version(c.getAnnotation(CachedInstrumented.class).version()), MxCache.getVersionObject());
        Assert.assertEquals(c.getAnnotation(CachedInstrumented.class).compatibleVersion(), MxCache.getCompatibleVersion());
        Assert.assertEquals(new Version(c.getAnnotation(UseProxyInstrumented.class).version()), MxCache.getVersionObject());
        Assert.assertEquals(c.getAnnotation(UseProxyInstrumented.class).compatibleVersion(), MxCache.getCompatibleVersion());
    }

    @Test(dataProvider = "all")
    public void testNoArgAndGroup(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        t.reset();

        assert t.get() == 0;
        assert t.get() == 0;

        getCleaner(cl).clearCacheByInstance(t);

        assert t.get() == 1;
        assert t.get() == 1;

        getCleaner(cl).clearCacheByGroup("g");

        assert t.get() == 2;
        assert t.get() == 2;
    }

    @Test(dataProvider = "all")
    public void testOneArgAndTag(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        t.reset();
        assert t.get(1) == 1;
        assert t.get(1) == 1;
        assert t.get(2) == 3;
        assert t.get(2) == 3;

        getCleaner(cl).clearCacheByInstance(t);

        assert t.get(1) == 3;
        assert t.get(1) == 3;
        assert t.get(2) == 5;
        assert t.get(2) == 5;

        getCleaner(cl).clearCacheByTag("t");

        assert t.get(1) == 5;
        assert t.get(1) == 5;
        assert t.get(2) == 7;
        assert t.get(2) == 7;
    }

    @Test(dataProvider = "all")
    public void testProbe(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        final TestCached t = loadCached(instrumentator, cl);
        assertFalse(MxCache.probe(new Runnable() {
            @Override
            public void run() {
                t.get();
            }
        }));
        t.get();
        assertTrue(MxCache.probe(new Runnable() {
            @Override
            public void run() {
                assertEquals(t.get(), 0);
            }
        }));
    }

    @Test(dataProvider = "all")
    public void testProbeCallable(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        final TestCached t = loadCached(instrumentator, cl);
        try {
            MxCache.probe(new Callable<Integer>() {
                @Override
                public Integer call() {
                    return t.get();
                }
            });
            fail();
        } catch (ProbeFailedException e) {
            // expected
        }
        t.get();
        assertEquals(MxCache.probe(new Callable<Integer>() {
            @Override
            public Integer call() {
                return t.get();
            }
        }).intValue(), 0);
    }


    @Test(dataProvider = "all")
    public void testReadWriteCached(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = readWrite(loadCached(instrumentator, cl));
        t.reset();
        assert t.get(1) == 1;
        assert t.get(1) == 1;
        assert t.get(2) == 3;
        assert t.get(2) == 3;

        getCleaner(cl).clearCacheByInstance(t);

        assert t.get(1) == 3;
        assert t.get(1) == 3;
        assert t.get(2) == 5;
        assert t.get(2) == 5;

        getCleaner(cl).clearCacheByTag("t");

        assert t.get(1) == 5;
        assert t.get(1) == 5;
        assert t.get(2) == 7;
        assert t.get(2) == 7;
    }

    @Test(dataProvider = "v262")
    public void testTupleArgMultitag(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        t.reset();
        assert t.get(-1, 1) == 0;
        assert t.get(-1, 1) == 0;
        assert t.get(-1, 2) == 2;
        assert t.get(-1, 2) == 2;

        getCleaner(cl).clearCacheByInstance(t);

        assert t.get(-1, 1) == 2;
        assert t.get(-1, 1) == 2;
        assert t.get(-1, 2) == 4;
        assert t.get(-1, 2) == 4;

        getCleaner(cl).clearCacheByTag("t1");

        assert t.get(-1, 1) == 4;
        assert t.get(-1, 1) == 4;
        assert t.get(-1, 2) == 6;
        assert t.get(-1, 2) == 6;

        // shouldn't clear, no such tag!
        getCleaner(cl).clearCacheByTag("t");

        assert t.get(-1, 1) == 4;
        assert t.get(-1, 1) == 4;
        assert t.get(-1, 2) == 6;
        assert t.get(-1, 2) == 6;

        getCleaner(cl).clearCacheByTag("t2");

        assert t.get(-1, 1) == 6;
        assert t.get(-1, 1) == 6;
        assert t.get(-1, 2) == 8;
        assert t.get(-1, 2) == 8;

        getCleaner(cl).clearCacheByAnnotation(CacheCleaningAnnotation.class);

        assert t.get(-1, 1) == 8;
        assert t.get(-1, 1) == 8;
        assert t.get(-1, 2) == 10;
        assert t.get(-1, 2) == 10;
    }

    private CacheCleaner getCleaner(ClassLoader cl) {
        if (cl == null) {
            return CacheFactory.getCleaner();
        }
        return getCacheCleanerProxy(cl);
    }

    private CacheCleaner getCacheCleanerProxy(ClassLoader cl) {
        try {
            for (Method method : cl.loadClass(CacheFactory.class.getName()).getMethods()) {
                if (method.getName().equals("getCleaner")) {
                    final Object cleaner = method.invoke(null);
                    return (CacheCleaner) Proxy.newProxyInstance(CacheCleaner.class.getClassLoader(), new Class[] {CacheCleaner.class}, new InvocationHandler() {
                        @Override
                        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                            return cleaner.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(cleaner, args);
                        }
                    });
                }
            }
            throw new IllegalStateException("Cannot find getCleaner() in CacheFactory");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Test(dataProvider = "all")
    public void testStatic(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        Method m = c.getDeclaredMethod("getStatic");

        assert m.invoke(null).equals(0);
        assert m.invoke(null).equals(0);

        getCleaner(cl).clearCacheByTag("t");

        assert m.invoke(null).equals(1);
        assert m.invoke(null).equals(1);

        getCleaner(cl).clearCacheByClass(c);

        assert m.invoke(null).equals(2);
        assert m.invoke(null).equals(2);

        // shouldn't clear static caches when cleaning instance!
        getCleaner(cl).clearCacheByInstance(c.newInstance());

        assert m.invoke(null).equals(2);
        assert m.invoke(null).equals(2);

        getCleaner(cl).clearCacheByGroup("g");

        assert m.invoke(null).equals(3);
        assert m.invoke(null).equals(3);

        // shouldn't clear - no such group! 
        getCleaner(cl).clearCacheByGroup("xxx");

        assert m.invoke(null).equals(3);
        assert m.invoke(null).equals(3);
    }

    @Test(dataProvider = "all")
    public void testOverloadTransform(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        assertEquals(t.transform("test"), "(String)test");
        assertEquals(t.transform("tester"), "(String)test");
        assertEquals(t.transform(3456L), "(Long)3456");
        assertEquals(t.transform(3457L), "(Long)3456");
    }

    @Test(dataProvider = "all")
    public void testPrimitiveTransform(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        assertEquals(t.transformPrimitive(2), "2");
        assertEquals(t.transformPrimitive(3), "2");
        assertEquals(t.transformPrimitive(5), "5");
        assertEquals(t.transformPrimitive(4), "5");
    }

    @Test(dataProvider = "all")
    public void testPrimitiveObjectTransform(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        assertEquals(t.transformPrimitiveToString(1), "1");
        assertEquals(t.transformPrimitiveToString(2), "1");
        assertEquals(t.transformPrimitiveToString(3), "1");

        CacheFactory.getCleaner().clearCacheByInstance(t);

        assertEquals(t.transformPrimitiveToString(2), "2");
        assertEquals(t.transformPrimitiveToString(3), "2");
    }

    @Test(dataProvider = "all")
    public void testStringCache(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        assert t.getString().equals("a");
        assert t.getString().equals("a");

        getCleaner(cl).clearCacheByInstance(t);

        assert t.getString().equals("aa");
        assert t.getString().equals("aa");
    }

    @Test(dataProvider = "all", expectedExceptions = IOException.class)
    public void testExceptionTransparency(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        t.exceptionTest();
    }

    @Test(dataProvider = "all")
    public void testDoubleInstrumentation(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestResourced t = (TestResourced) instrumentClass(TestResourcedImpl.class, instrumentator, cl).newInstance();

        class TestRunnable implements Runnable {
            boolean run;

            @Override
            public void run() {
                MxResource res = MxResourceFactory.getResource("testResource");
                Assert.assertTrue(res.isReading());
                run = true;
            }
        }

        TestRunnable r = new TestRunnable();
        t.doWithRead(r);
        Assert.assertTrue(r.run);
    }

    @Test(dataProvider = "v262")
    public void testStringTupleCache(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        assert t.test("a", "b").equals("ab0");
        assert t.test("a", "b").equals("ab0");

        getCleaner(cl).clearCacheByInstance(t);

        assert t.test("a", "b").equals("ab1");
        assert t.test("a", "b").equals("ab1");
    }

    @Test(dataProvider = "all")
    public void testStaticField(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        assert instrumentClass(TestCachedImpl.class, instrumentator, cl).getField("test").get(null).equals("TEST");
    }

    @Test(dataProvider = "all")
    public void testProxied0Arg(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.test(), "1230");
        assertEquals(t.test(), "1231");
        t.setPrefix("123");
        assertEquals(t.test(), "1231232");
    }

    @Test(dataProvider = "all")
    public void testProxiedInvalid(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        // если нельзя инстанцировать фабрику проксей, будет использоваться пустая фабрика
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.testInvalid(), "123");
        assertEquals(t.testInvalid(), "123");
        t.setPrefix("123");
        assertEquals(t.testInvalid(), "123123");
    }

    @Test(dataProvider = "all")
    public void testProxied1Arg(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.test("123"), "1230");
        assertEquals(t.test("456"), "4561");
        assertEquals(t.test("123"), "1232");
        t.setPrefix("123");
        assertEquals(t.test("123"), "1231233");
        assertEquals(t.test("456"), "1234564");
    }

    @Test(dataProvider = "all")
    public void testProxied2Arg(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.test("123", "A"), "123A0");
        assertEquals(t.test("456", "B"), "456B1");
        assertEquals(t.test("123", "C"), "123C2");
        t.setPrefix("123");
        assertEquals(t.test("123", "D"), "123123D3");
        assertEquals(t.test("456", "E"), "123456E4");
    }

    @Test(dataProvider = "all")
    public void testProxiedStatic1Arg(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        Method m = t.getClass().getMethod("testStatic", String.class);
        assertEquals(m.invoke(null, "123"), "1230");
        assertEquals(m.invoke(null, "456"), "4561");
        assertEquals(m.invoke(null, "123"), "1232");
        assertEquals(m.invoke(null, "123"), "1233");
        assertEquals(m.invoke(null, "456"), "4564");
    }

    @Test(dataProvider = "all")
    public void testCachedAndProxiedConflicts(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        t.setPrefix("X");
        assertEquals(t.justCached("test"), "XtestC");
        assertEquals(t.justCached("???"), "X???C");
        t.setPrefix("Y");
        assertEquals(t.justCached("test"), "XtestC");
        assertEquals(t.justCached("???"), "X???C");
        assertEquals(t.justCached("!!!"), "Y!!!C");
    }

    @Test(dataProvider = "all")
    public void testBothCachedAndProxied(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        t.setPrefix("X");
        assertEquals(t.cachedAndProxied("test"), "XtestC0");
        assertEquals(t.cachedAndProxied("???"), "X???C1");
        t.setPrefix("Y");
        assertEquals(t.cachedAndProxied("test"), "XtestC2");
        assertEquals(t.cachedAndProxied("???"), "X???C3");
        assertEquals(t.cachedAndProxied("!!!"), "Y!!!C4");
    }

    @Test(dataProvider = "all")
    public void testProxiedTransform(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.transform("test"), "test_T0");
        assertEquals(t.transform("test"), "test_T1");
        assertEquals(t.transform("test"), "test_T2");
    }

    @Test(dataProvider = "all")
    public void testProxiedTransformWithInstance(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        DefaultInstanceProvider.getInstance().bind(TestTransforms.class).toInstance(new TestTransforms("?"));
        TestProxied t = loadProxied(instrumentator, cl);
        assertEquals(t.transformWithInstance("test"), "test?T0");
        assertEquals(t.transformWithInstance("test"), "test?T1");
        assertEquals(t.transformWithInstance("test"), "test?T2");
    }

    @Test(dataProvider = "all")
    public void testProxiedReadWrite(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestProxied t = readWrite(loadProxied(instrumentator, cl));
        assertEquals(t.test("123"), "1230");
        assertEquals(t.test("456"), "4561");
        assertEquals(t.test("123"), "1232");
        t.setPrefix("123");
        assertEquals(t.test("123"), "1231233");
        assertEquals(t.test("456"), "1234564");
    }

    @Test(dataProvider = "v262")
    public void testIgnore(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);
        assertEquals(t.ignore("123", "456"), "123456");
        assertEquals(t.ignore("123", "666"), "123456");

        assertEquals(t.ignore("123"), "123");
        assertEquals(t.ignore("333"), "123");

        assertEquals(t.ignore("12", 3), "123");
        assertEquals(t.ignore("33", 3), "123");

        // we want key to be gc'ed
        // noinspection RedundantStringConstructorCall
        String key = new String("23");
        assertEquals(t.ignore(1, key), "123");
        assertEquals(t.ignore(2, "23"), "123");

        assertEquals(t.ignore("A", 1, key), "A123");
        assertEquals(t.ignore("A", 2, "23"), "A123");

        // we want key to be gc'ed, and create new key
        key = "23";

        System.gc();
        Thread.sleep(100);
        System.gc();

        assertEquals(t.ignore(2, key), "223");
        assertEquals(t.ignore("A", 2, key), "A223");
    }

    private static class DefaultTestStrategy extends TestStrategy {
        @Nonnull
        @Override
        public CacheManager getManager(CacheContext context, Class<?> ownerClass,  CacheDescriptor descriptor) {
            return DefaultStrategy.getInstance().getManager(context, ownerClass, descriptor);
        }
    }

    private static class SpyCacheProvider implements CacheProvider {
        private final CacheProvider provider0;

        private final List<Object[]> queries = new ArrayList<Object[]>();

        public SpyCacheProvider(CacheProvider provider0) {
            this.provider0 = provider0;
        }

        @Override
        public void registerCache(Class<?> cacheOwner, int cacheId, Class keyType, Class valueType, String group, String[] tags, Calculable calculable, String methodName, String methodDesc, @Nullable String cacheName) {
            queries.add(new Object[] {"registerCache", cacheOwner, cacheId, keyType, valueType, group, tags, calculable, methodName, methodDesc, cacheName});
            provider0.registerCache(cacheOwner, cacheId, keyType, valueType, group, tags, calculable, methodName, methodDesc, cacheName);
        }

        @Override
        public Cache createCache(@Nonnull Class cacheOwner, int cacheId, @Nullable Object instance, CacheContext context) {
            queries.add(new Object[] {"createCache", cacheOwner, cacheId, instance, context});
            return provider0.createCache(cacheOwner, cacheId, instance, context);
        }

        @Override
        public CacheDescriptor getDescriptor(CacheId id) {
            return provider0.getDescriptor(id);
        }

        @Override
        public List<CacheManager> getCaches() {
            return provider0.getCaches();
        }

        @Override
        public void intercept(CacheProviderInterceptor interceptor) {
            provider0.intercept(interceptor);
        }

        @Override
        public boolean removeInterceptor(CacheProviderInterceptor interceptor) {
            return provider0.removeInterceptor(interceptor);
        }

        public List<Object[]> getQueries() {
            return queries;
        }
    }

    private class OverrideInstanceProvider implements InstanceProvider {
        @SuppressWarnings( { "unchecked" })
        @Nonnull
        @Override
        public <T> T forClass(@Nonnull Class<T> cls) {
            if (cls == TestStrategy.class) {
                return (T) new DefaultTestStrategy();
            }
            return CacheFactory.getDefaultContext().getInstanceProvider().forClass(cls);
        }
    }

    private static <T> T serialize(T proxy) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(proxy);
        } finally {
            oos.close();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        final Class<?> replacedClass = proxy.getClass();

        ObjectInputStream ois = new ObjectInputStream(bis) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if (desc.getName().equals(replacedClass.getName())) {
                    return replacedClass;
                }
                return super.resolveClass(desc);
            }
        };
        try {
            //noinspection unchecked
            return (T) ois.readObject();
        } finally {
            ois.close();
            bis.close();
            bos.close();
        }
    }

    @Test(dataProvider = "all")
    public void testView(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Point p = loadPoint(instrumentator, cl);
        p.setX(3L);
        p.setY(4L);
        assertEquals(p.getRadius(), 5.0);

        p.setNewXY(4L, 3L);
        assertEquals(p.getRadius(), 5.0);

        p.setX(4L);
        assertEquals(p.getRadius(), 5.0);

        p.setX(0L);
        assertEquals(p.getRadius(), 3.0);
    }

    @SuppressWarnings("RedundantStringConstructorCall")
    @Test(dataProvider = "v262")
    public void testArrays(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        TestCached t = loadCached(instrumentator, cl);

        assertEquals(t.getByArray(new int[] {1}), t.getByArray(new int[] {1}));
        assertEquals(t.getByArray(new long[] {1}), t.getByArray(new long[] {1}));
        assertEquals(t.getByArray(new short[] {1}), t.getByArray(new short[] {1}));
        assertEquals(t.getByArray(new double[] {1}), t.getByArray(new double[] {1}));
        assertEquals(t.getByArray(new float[] {1}), t.getByArray(new float[] {1}));
        assertEquals(t.getByArray(new byte[] {1}), t.getByArray(new byte[] {1}));
        assertEquals(t.getByArray(new boolean[] {true}), t.getByArray(new boolean[] {true}));
        assertEquals(t.getByArray(new char[] {'f'}), t.getByArray(new char[] {'f'}));
        assertEquals(t.getByArray(new Object[] {new String("1")}), t.getByArray(new Object[] {new String("1")}));
        assertEquals(t.getByArray(1, new int[]{2}), t.getByArray(1, new int[] {2}));
        assertEquals(t.getByArray(2, new long[]{2}), t.getByArray(2, new long[] {2}));
        assertEquals(t.getByArray(new String("ы"), new short[]{1}), t.getByArray(new String("ы"), new short[] {1}));
        assertEquals(t.getByArray(new Object[] {new String("ы")}, new double[] {2}), t.getByArray(new Object[] {new String("ы")}, new double[] {2}));
        assertEquals(t.getByArray(new float[]{2}, 3), t.getByArray(new float[]{2}, 3));
        assertNotEquals(t.getByArrayIdentityStr(new byte[]{1}, new String("ы")), t.getByArrayIdentityStr(new byte[]{1}, new String("ы")));
        assertNotEquals(t.getByArrayIdentity(new boolean[]{true}), t.getByArrayIdentity(new boolean[] {true}));
        assertEquals(t.getByArraySameStrategy(new char[] {'f'}), t.getByArraySameStrategy(new char[] {'f'}));
        assertNotEquals(t.getByArrayIdentity2(new Object[]{new String("1")}, new Object[] {new String("1")}), t.getByArrayIdentity2(new Object[] {new String("1")}, new Object[] {new String("1")}));
        assertNotEquals(t.getSingleByIdentity(new String("ы")), t.getSingleByIdentity(new String("ы")));
    }
}
