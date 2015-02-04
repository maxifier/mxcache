/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;
import com.maxifier.mxcache.tuple.Tuple;
import com.maxifier.mxcache.tuple.TupleGenerator;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.getType;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
@SuppressWarnings({ "UnusedDeclaration" })
public class TransformGeneratorManagerUTest {
    private static final Type CACHE_FACTORY_TYPE = getType(CacheFactory.class);
    private static final Type CACHE_CONTEXT_TYPE = getType(CacheContext.class);

    private final TransformGeneratorFactoryImpl instance = TransformGeneratorFactoryImpl.getInstance();
    public static final String TEST_CLASS_NAME = "$$$Test$$$";
    public static final Method NO_ARG_CONSTRUCTOR = Method.getMethod("void <init>()");

    private static final Type UNSUPPORTED_OPERATION_EXCEPTION_TYPE = Type.getType(UnsupportedOperationException.class);

    private static final Annotation[] EMPTY_ANNOTATIONS = {};

    private static class Test1 {
        public void get() {}

        public void set() {}

        public static void who(Test1 x) {}

        // private method should not be considered ambiguous.
        private static void who(Object y) {}

        public static void who(String y) {}

        public void where() {}

        // Этот метод нам не мешает, т.к. он не статический, а не-статические методы с параметром у ключа не проверяются
        public void where(Test1 t) {}

        public static void where(String x) {}
    }

    private static class Test2 {
        public void onlyMethod() {}

        // private method should not be considered ambiguous.
        private void onlyMethod(int x) {}

        // object methods should not be considered ambiguous to only public method.
        public String toString() { return "test"; }
    }

    private static class Test3 extends Test2 {
        // inherited class still has only public method
    }

    public void testKeyFindMethod() throws NoSuchMethodException {
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test1.class, "toString", Test1.class, true), Test1.class.getMethod("toString"));
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test1.class, "get", Test1.class, true), Test1.class.getMethod("get"));
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test1.class, "set", Test1.class, true), Test1.class.getMethod("set"));
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test1.class, "who", Test1.class, true), Test1.class.getMethod("who", Test1.class));
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test1.class, "where", Test1.class, true), Test1.class.getMethod("where"));
    }

    public void testFindOnlyMethod() throws NoSuchMethodException {
        // private method get(int) should not be considered ambiguous.
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test2.class, null, Test2.class, true), Test2.class.getMethod("onlyMethod"));
        Assert.assertEquals(TransformGeneratorFactoryImpl.findMethod(Test3.class, null, Test3.class, true), Test3.class.getMethod("onlyMethod"));
    }

    private static class TestAmbiguous1 {
        public void get(String s) {}

        public void get(Object y) {}

        public void where() {}

        public static void where(TestAmbiguous1 x) {}
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindMethodAmbiguous() throws NoSuchMethodException {
        TransformGeneratorFactoryImpl.findMethod(TestAmbiguous1.class, "get", String.class, false);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindMethodKeyAmbiguous() throws NoSuchMethodException {
        TransformGeneratorFactoryImpl.findMethod(TestAmbiguous1.class, "where", TestAmbiguous1.class, true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindMethodNoMethodsOnly() throws NoSuchMethodException {
        TransformGeneratorFactoryImpl.findMethod(Object.class, null, Object.class, false);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFindMethodNoMethodsNamed() throws NoSuchMethodException {
        TransformGeneratorFactoryImpl.findMethod(Object.class, "xxx", Object.class, false);
    }

    public interface TransformTest<From, To> {
        public To forward(From from);

        public From backward(To from);
    }

    private static Type getBoxedType(final Type type) {
        switch(type.getSort()) {
            case Type.BYTE:
                return Type.getObjectType("java/lang/Byte");
            case Type.BOOLEAN:
                return Type.getObjectType("java/lang/Boolean");
            case Type.SHORT:
                return Type.getObjectType("java/lang/Short");
            case Type.CHAR:
                return Type.getObjectType("java/lang/Character");
            case Type.INT:
                return Type.getObjectType("java/lang/Integer");
            case Type.FLOAT:
                return Type.getObjectType("java/lang/Float");
            case Type.LONG:
                return Type.getObjectType("java/lang/Long");
            case Type.DOUBLE:
                return Type.getObjectType("java/lang/Double");
        }
        return type;
    }

    @SuppressWarnings({ "unchecked" })
    private <K, V> TransformTest<K, V> generateTestTransform(TransformGenerator t, Class<K> input) throws IllegalAccessException, InstantiationException {
        ClassGenerator w = new ClassGenerator(ACC_PUBLIC | ACC_SUPER, TEST_CLASS_NAME, OBJECT_TYPE, Type.getType(TransformTest.class));

        Type testType = Type.getObjectType(TEST_CLASS_NAME);

        t.generateFields(testType, 0, w);

        MxConstructorGenerator ctor = w.defineConstructor(ACC_PUBLIC);
        ctor.callSuper();
        int local = ctor.newLocal(CACHE_CONTEXT_TYPE);

        ctor.invokeStatic(Type.getType(CacheFactory.class), new Method("getDefaultContext", CACHE_CONTEXT_TYPE, EMPTY_TYPES));
        ctor.storeLocal(local);

        t.generateAcquire(testType, 0, ctor, local);
        ctor.returnValue();
        ctor.endMethod();

        Class output = t.getOutType();

        MxGeneratorAdapter forward = w.defineMethod(ACC_PUBLIC, Method.getMethod("Object forward(Object)"));
        forward.start();
        forward.loadArg(0);
        if (input.isPrimitive()) {
            forward.unbox(Type.getType(input));
        } else {
            forward.checkCast(Type.getType(input));
        }
        t.generateForward(testType, 0, forward);
        if (output.isPrimitive()) {
            forward.box(Type.getType(output));
        }
        forward.returnValue();
        forward.endMethod();

        MxGeneratorAdapter backward = w.defineMethod(ACC_PUBLIC, Method.getMethod("Object backward(Object)"));
        backward.start();
        backward.loadArg(0);
        if (output.isPrimitive()) {
            backward.unbox(Type.getType(output));
        } else {
            backward.checkCast(Type.getType(output));
        }
        try {
            t.generateBackward(testType, 0, backward);

            if (input.isPrimitive()) {
                backward.box(Type.getType(input));
            }
            backward.returnValue();
        } catch (UnsupportedOperationException e) {
            backward.throwException(UNSUPPORTED_OPERATION_EXCEPTION_TYPE, e.getMessage());
        }
        backward.endMethod();

        w. visitEnd();

        return (TransformTest) loadClass(new ClassLoader() {}, w.toByteArray()).newInstance();
    }

    public static String test1(String s) {
        return s + s;
    }

    public char test2(String s) {
        return s.charAt(0);
    }

    public interface X {
        int test3(String s);
    }

    public static class XImpl implements X {
        @Override
        public int test3(String s) {
            return Integer.parseInt(s);
        }
    }

    public double test4(double s) {
        return s * 2;
    }

    @CustomTransformAnnotation(transformGenerator = TestFactory.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface CustomTransform {
    }

    @ReversibleTransform(
            forward = @Transform(owner = Long.class, method = "parseLong"),
            backward = @Transform(owner = Long.class, method = "toString")
    )
    @Retention(RetentionPolicy.RUNTIME)
    public @interface LongAsString {
    }

    public static class TestFactory implements TransformFactory {
        @Override
        public TransformGenerator create(Annotation annotation, Annotation[] allAnnotations, Class paramType) {
            return new ScalarTransformGenerator() {
                @Override
                public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
                    method.pop();
                    method.push("TEST");
                }

                @Override
                public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Class getOutType() {
                    return String.class;
                }

                @Override
                public Class getInType() {
                    return Object.class;
                }
            };
        }
    }


    @SuppressWarnings({ "UnusedDeclaration" })
    private static void testParams(@Transform(owner = TransformGeneratorManagerUTest.class, method = "test1") int a,
                                   @Transform(method = "toLowerCase") int b,
                                   @Transform(method = "length") int c,
                                   @Transform(owner = TransformGeneratorManagerUTest.class, method = "test2") int d,
                                   @Transform(owner = X.class) int e,
                                   @Transform(owner = TransformGeneratorManagerUTest.class, method = "test4") int f,
                                   @Transform(owner = Integer.class, method = "toString") int g,
                                   @Transform(owner = TransformGeneratorManagerUTest.class) int h,
                                   @WeakKey int i,
                                   @SoftKey int j,
                                   @Transform(owner = TransformGeneratorManagerUTest.class, method = "test1") @WeakKey int k,
                                   @WeakKey @SoftKey int l,
                                   @CustomTransform int m,
                                   @ReversibleTransform(forward = @Transform(owner = Long.class, method = "parseLong"),
                                                        backward = @Transform(owner = Long.class, method = "toString")) int n,
                                   @LongAsString int o) {}
    private static final Annotation[][] TEST_PARAMS = getTestParams();

    private static Annotation[][] getTestParams() {
        for (java.lang.reflect.Method m : TransformGeneratorManagerUTest.class.getDeclaredMethods()) {
            if (m.getName().equals("testParams")) {
                return m.getParameterAnnotations();
            }
        }
        throw new IllegalStateException("No testParams(...) found");
    }

    @Test
    public void testStaticTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<String, String> t = generateTestTransform(instance.forArgument(TEST_PARAMS[0], String.class), String.class);
        Assert.assertEquals(t.forward("123"), "123123");
    }

    @Test
    public void testKeyVirtualTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<String, String> t = generateTestTransform(instance.forArgument(TEST_PARAMS[1], String.class), String.class);
        Assert.assertEquals(t.forward("ABc"), "abc");
    }

    @Test
    public void testKeyInterfaceTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<CharSequence, Integer> t = generateTestTransform(instance.forArgument(TEST_PARAMS[2], CharSequence.class), CharSequence.class);
        Assert.assertEquals(t.forward("ABc"), Integer.valueOf(3));
    }

    @Test
    public void testVirtualTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<String, Character> t = generateTestTransform(instance.forArgument(TEST_PARAMS[3], String.class), String.class);
        Assert.assertEquals(t.forward("ABc"), Character.valueOf('A'));
    }

    @Test
    public void testInterfaceTransform() throws InstantiationException, IllegalAccessException {
        DefaultInstanceProvider.getInstance().bind(X.class).toClass(XImpl.class);
        TransformTest<String, Integer> t = generateTestTransform(instance.forArgument(TEST_PARAMS[4], String.class), String.class);
        Assert.assertEquals(t.forward("31"), Integer.valueOf(31));
    }

    @Test
    public void testVirtualDoubleSlotTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<Double, Double> t = generateTestTransform(instance.forArgument(TEST_PARAMS[5], double.class), double.class);
        Assert.assertEquals(t.forward(31d), 62d);
    }

    @Test
    public void testTupleSimpleTransform() throws InstantiationException, IllegalAccessException {
        Class[] keys = { String.class, String.class };
        Class<Tuple> tupleClass = TupleGenerator.getTupleClass(keys);
        TransformTest<Tuple, Tuple> t = generateTestTransform(instance.createMultiParam(new Annotation[][] {TEST_PARAMS[0], TEST_PARAMS[1]}, keys), tupleClass);
        Assert.assertEquals(t.forward(TupleGenerator.getTupleFactory(keys).create("AAA", "Abs")), TupleGenerator.getTupleFactory(keys).create("AAAAAA", "abs"));
    }

    @Test
    public void testTupleSimpleTransformWithUntransformedParams() throws InstantiationException, IllegalAccessException {
        Class[] keys = { String.class, String.class, String.class };
        Class<Tuple> tupleClass = TupleGenerator.getTupleClass(keys);
        TransformTest<Tuple, Tuple> t = generateTestTransform(instance.createMultiParam(new Annotation[][] { TEST_PARAMS[0], EMPTY_ANNOTATIONS, TEST_PARAMS[1] }, keys), tupleClass);
        Assert.assertEquals(t.forward(TupleGenerator.getTupleFactory(keys).create("AAA", "1223", "Abs")), TupleGenerator.getTupleFactory(keys).create("AAAAAA", "1223", "abs"));
    }

    @Test
    public void testTuplePrimitiveTransform() throws InstantiationException, IllegalAccessException {
        Class[] keysFrom = { String.class, int.class };
        Class[] keysTo = { int.class, String.class };
        Class<Tuple> tupleFrom = TupleGenerator.getTupleClass(keysFrom);
        Class<Tuple> tupleTo = TupleGenerator.getTupleClass(keysFrom);
        TransformTest<Tuple, Tuple> t = generateTestTransform(instance.createMultiParam(new Annotation[][] { TEST_PARAMS[2], TEST_PARAMS[6] }, keysFrom), tupleFrom);
        Assert.assertEquals(t.forward(TupleGenerator.getTupleFactory(keysFrom).create("AAA", 777)), TupleGenerator.getTupleFactory(keysTo).create(3, "777"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidTransform() throws InstantiationException, IllegalAccessException {
        generateTestTransform(instance.forArgument(TEST_PARAMS[7], String.class), String.class);
    }

    public void testWeakTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<String, WeakReference<String>> t = generateTestTransform(instance.forArgument(TEST_PARAMS[8], String.class), String.class);
        Assert.assertEquals(t.forward("123").get(), "123");
    }

    public void testSoftTransform() throws InstantiationException, IllegalAccessException {
        TransformTest<String, SoftReference<String>> t = generateTestTransform(instance.forArgument(TEST_PARAMS[9], String.class), String.class);
        Assert.assertEquals(t.forward("123").get(), "123");
    }

    @Test(expectedExceptions = InvalidTransformAnnotations.class)
    public void testInvalidMixedCustomAndDefaultTransform() throws InstantiationException, IllegalAccessException {
        generateTestTransform(instance.forArgument(TEST_PARAMS[10], String.class), String.class);
    }

    @Test(expectedExceptions = InvalidTransformAnnotations.class)
    public void testInvalidMultipleCustomTransform() throws InstantiationException, IllegalAccessException {
        generateTestTransform(instance.forArgument(TEST_PARAMS[11], String.class), String.class);
    }

    @Test
    public void testCustomTransformGenerator() throws InstantiationException, IllegalAccessException {
        TransformTest<String, String> t = generateTestTransform(instance.forArgument(TEST_PARAMS[12], String.class), String.class);
        Assert.assertEquals(t.forward("123"), "TEST");
    }

    @Test
    public void testReversible() throws InstantiationException, IllegalAccessException {
        TransformTest<String, Long> t = generateTestTransform(instance.forArgument(TEST_PARAMS[13], String.class), String.class);
        Assert.assertEquals(t.forward("123"), Long.valueOf(123));
        Assert.assertEquals(t.backward(123L), "123");
    }

    @Test
    public void testIndirectReversible() throws InstantiationException, IllegalAccessException {
        TransformTest<String, Long> t = generateTestTransform(instance.forArgument(TEST_PARAMS[14], String.class), String.class);
        Assert.assertEquals(t.forward("123"), Long.valueOf(123));
        Assert.assertEquals(t.backward(123L), "123");
    }

    @Test
    public void testTupleReversible() throws InstantiationException, IllegalAccessException {
        Class[] keys = { String.class, String.class, String.class };
        Class[] res = { long.class, String.class, long.class };
        Class<Tuple> tupleClass = TupleGenerator.getTupleClass(keys);
        TransformTest<Tuple, Tuple> t = generateTestTransform(instance.createMultiParam(new Annotation[][] { TEST_PARAMS[13], EMPTY_ANNOTATIONS, TEST_PARAMS[14] }, keys), tupleClass);
        Assert.assertEquals(t.forward(TupleGenerator.getTupleFactory(keys).create("444", "1223", "112")),
                            TupleGenerator.getTupleFactory(res).create(444L, "1223", 112L));

        Assert.assertEquals(t.backward(TupleGenerator.getTupleFactory(res).create(444L, "1223", 112L)),
                            TupleGenerator.getTupleFactory(keys).create("444", "1223", "112"));
    }
}
