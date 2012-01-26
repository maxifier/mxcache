package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.CacheContextImpl;
import com.maxifier.mxcache.impl.DefaultStrategy;
import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import com.maxifier.mxcache.clean.CacheCleaner;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.util.CodegenHelper;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.*;
import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 8:47:52
 */
@Test
public class DynamicInstrumentationFTest {
    /**
     * It's a bit hacky cause we want to load out own implementation of commons, but we also need some classes from
     * tests.
     */
//    private static final ClassLoader ISOLATED_CLASSLOADER = new ClassLoader() {
//        @Override
//        public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
//            if (name.equals(TestCached.class.getName())) {
//                return TestCached.class;
//            }
//            if (name.equals(TestProxied.class.getName())) {
//                return TestProxied.class;
//            }
//            if (name.startsWith("com.magenta.mxcache2.") || name.startsWith("com.maxifier.mxcache.")) {
//                // exclude references to mxcache classes, but allow utility classes (trove, apache commons, etc.)
//                // tuples are created in system classloader, so we need all commons to have same TupleGenerator.
//                throw new ClassNotFoundException();
//            }
//            return super.loadClass(name, resolve);
//        }
//    };

//    private static final String ASM_JAR_NAME = "mxcache-asm-2.1.8.jar";
//    private static final String COMMONS_2_0_1_JAR_NAME = "mxcache-commons-2.0.1.jar";
//    private static final String COMMONS_2_1_0_JAR_NAME = "mxcache-commons-2.1.0.jar";
    
//    private static final ClassLoader V200_CLASSLOADER = getArtifact(COMMONS_2_0_1_JAR_NAME, ASM_JAR_NAME);
//    private static final ClassLoader V210_CLASSLOADER = getArtifact(COMMONS_2_1_0_JAR_NAME, ASM_JAR_NAME);
    
//    private static ClassLoader getArtifact(String... names) {
//        URL[] urls = new URL[names.length];
//        for (int i = 0; i<names.length; i++) {
//            urls[i] = DynamicInstrumentationFTest.class.getClassLoader().getResource(names[i]);
//        }
//        return new URLClassLoader(urls, ISOLATED_CLASSLOADER);
//    }

//    private static final Object[] V200 = { Instrumentator200.INSTANCE, V200_CLASSLOADER };
//    private static final Object[] V210 = { Instrumentator210.INSTANCE, V210_CLASSLOADER};
    private static final Object[] V219 = { InstrumentatorImpl.INSTANCE_219, null};
    private static final Object[] V229 = { InstrumentatorImpl.INSTANCE_229, null };

    @DataProvider(name = "v229")
    public Object[][] v229() {
        return new Object[][] { V229 };
    }

    @DataProvider(name = "v219")
    public Object[][] v219() {
        return new Object[][] { V219 };
    }

    @DataProvider(name = "all")
    public Object[][] all() {
        return new Object[][] { V219, V229 };
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

    @Test(dataProvider = "v229")
    public void testModifiersRegressionMxcache31(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        Assert.assertTrue((c.getDeclaredMethod("get").getModifiers() & Opcodes.ACC_SYNTHETIC) != 0, "Generated method should be synthetic");
        Assert.assertTrue((c.getDeclaredMethod("get$create").getModifiers() & Opcodes.ACC_SYNTHETIC) == 0, "Original method should not be synthetic");
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    @Test(dataProvider = "all")
    public void testNonCached(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        // тут сработает отсечение на дескриптор
        assert instrumentator.instrument(CodegenHelper.getByteCode(NonCachedShortcut.class)) == null;
        // а тут сработает отсечение на отсутствие кэшированных методов
        assert instrumentator.instrument(CodegenHelper.getByteCode(NotCached.class)) == null;
    }

    @Test(dataProvider = "v229")
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

    @Test(dataProvider = "v229")
    public void testCustomContextSerialize(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestCachedImpl.class, instrumentator, cl);
        TestCached defInstance = (TestCached) c.newInstance();
        Assert.assertEquals(defInstance.nullCache("test"), "test0");
        Assert.assertEquals(defInstance.nullCache("test"), "test1");
        Assert.assertEquals(defInstance.nullCache("test"), "test2");

        Assert.assertEquals(defInstance.nullCache("past"), "past3");
        Assert.assertEquals(defInstance.nullCache("past"), "past4");
        Assert.assertEquals(defInstance.nullCache("past"), "past5");

        CacheContextImpl context = new CacheContextImpl(new OverrideInstanceProvider());

        TestCached cusInstance = defInstance.reloadWithContext(context);

        Assert.assertEquals(cusInstance.nullCache("test"), "test6");
        Assert.assertEquals(cusInstance.nullCache("test"), "test6");
        Assert.assertEquals(cusInstance.nullCache("test"), "test6");

        Assert.assertEquals(cusInstance.nullCache("past"), "past7");
        Assert.assertEquals(cusInstance.nullCache("past"), "past7");
        Assert.assertEquals(cusInstance.nullCache("past"), "past7");

    }

    @Test(dataProvider = "v229")
    public void testMarkerAnnotations(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestProxiedImpl.class, instrumentator, cl);
        Assert.assertEquals(c.getAnnotation(CachedInstrumented.class).version(), MxCache.getVersion());
        Assert.assertEquals(c.getAnnotation(CachedInstrumented.class).compatibleVersion(), MxCache.getCompatibleVersion());
        Assert.assertEquals(c.getAnnotation(UseProxyInstrumented.class).version(), MxCache.getVersion());
        Assert.assertEquals(c.getAnnotation(UseProxyInstrumented.class).compatibleVersion(), MxCache.getCompatibleVersion());
    }

    @Test(dataProvider = "v219")
    public void testNoMarkerAnnotationsIn219(Instrumentator instrumentator, ClassLoader cl) throws Exception {
        Class<?> c = instrumentClass(TestProxiedImpl.class, instrumentator, cl);
        Assert.assertNull(c.getAnnotation(CachedInstrumented.class));
        Assert.assertNull(c.getAnnotation(UseProxyInstrumented.class));
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

    @Test(dataProvider = "all")
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

    @Test(dataProvider = "all")
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

    @Test(dataProvider = "all")
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
        @NotNull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            return DefaultStrategy.getInstance().getManager(context, descriptor);
        }
    }

    private class OverrideInstanceProvider implements InstanceProvider {
        @SuppressWarnings( { "unchecked" })
        @NotNull
        @Override
        public <T> T forClass(@NotNull Class<T> cls) {
            if (cls == TestStrategy.class) {
                return (T) new DefaultTestStrategy();
            }
            return CacheFactory.getDefaultContext().getInstanceProvider().forClass(cls);
        }
    }
}
