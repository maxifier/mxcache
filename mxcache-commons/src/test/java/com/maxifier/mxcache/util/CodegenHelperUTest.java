package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.Type;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static org.testng.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.04.2010
 * Time: 13:37:24
 */
@Test
public class CodegenHelperUTest {
    @Test
    public void testGetClassName() throws Exception {
        byte[] bytecode = CodegenHelper.getByteCode(CodegenHelperUTest.class);
        assertEquals(Type.getType(CodegenHelperUTest.class).getInternalName(), CodegenHelper.getClassName(bytecode));
    }

    @Test
    public void testLoadClass() throws Exception {
        byte[] bytecode = CodegenHelper.getByteCode(TestClassImpl.class);
        TestClass instance = (TestClass) CodegenHelper.loadClass(new ClassLoader() {}, bytecode).newInstance();
        assertEquals(instance.test(), "testString");
    }

    @Test
    public void testGetMethod() throws Exception {
        Method waitMethod = Object.class.getDeclaredMethod("wait", long.class, int.class);
        assertEquals(CodegenHelper.getMethod(Object.class, "wait", "(JI)V"), waitMethod);
    }

    @Test
    public void testGetMethodAnotherClassLoader() throws Exception {
        ClassLoader x = new ClassLoader() {};

        ClassGenerator g1 = new ClassGenerator(ACC_PUBLIC, "$test1", Object.class);
        g1.defineDefaultConstructor();
        Class<Object> g1c = CodegenHelper.loadClass(x, g1.toByteArray());

        ClassGenerator g2 = new ClassGenerator(ACC_PUBLIC, "$test2", Object.class);
        g2.defineDefaultConstructor();
        MxGeneratorAdapter m = g2.defineMethod(ACC_PUBLIC, "x", g1.getThisType(), g1.getThisType());
        m.start();
        m.pushNull();
        m.returnValue();
        m.endMethod();

        Class g2c = CodegenHelper.loadClass(x, g2.toByteArray());

        assertEquals(CodegenHelper.getMethod(g2c, "x", "(L$test1;)L$test1;"), g2c.getMethod("x", g1c));
    }

    private static class InnerTest {}

    @Test
    public void testToClass() {
        testToClass(void.class);
        testToClass(boolean.class);
        testToClass(byte.class);
        testToClass(short.class);
        testToClass(char.class);
        testToClass(int.class);
        testToClass(long.class);
        testToClass(float.class);
        testToClass(double.class);
        testToClass(Void.class);
        testToClass(String.class);
        testToClass(Long.class);
        testToClass(Object.class);
        testToClass(Runnable.class);

        testToClass(int[].class);
        testToClass(short[].class);
        testToClass(String[].class);
        testToClass(String[][].class);

        testToClass(InnerTest.class);
        //noinspection InstantiatingObjectToGetClassObject
        testToClass(new Object() {}.getClass());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailedToClass() throws ClassNotFoundException {
        CodegenHelper.toClass(getClass().getClassLoader(), Type.getObjectType("xxx"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailedArrayToClass() throws ClassNotFoundException {
        CodegenHelper.toClass(getClass().getClassLoader(), Type.getType("[Lxxx;"));
    }

    private void testToClass(Class x) {
        assertEquals(CodegenHelper.toClass(getClass().getClassLoader(), Type.getType(x)), x);
    }
}
