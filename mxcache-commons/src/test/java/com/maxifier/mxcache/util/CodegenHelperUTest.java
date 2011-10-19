package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.Type;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

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
        assert Type.getType(CodegenHelperUTest.class).getInternalName().equals(CodegenHelper.getClassName(bytecode));
    }

    @Test
    public void testLoadClass() throws Exception {
        byte[] bytecode = CodegenHelper.getByteCode(TestClassImpl.class);
        TestClass instance = (TestClass) CodegenHelper.loadClass(new ClassLoader() {}, bytecode).newInstance();
        assert instance.test().equals("testString");
    }

    @Test
    public void testGetMethod() throws Exception {
        Method waitMethod = Object.class.getDeclaredMethod("wait", long.class, int.class);
        assert CodegenHelper.getMethod(Object.class, "wait", "(JI)V").equals(waitMethod);
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
        CodegenHelper.toClass(Type.getObjectType("xxx"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFailedArrayToClass() throws ClassNotFoundException {
        CodegenHelper.toClass(Type.getType("[Lxxx;"));
    }

    private void testToClass(Class x) {
        Assert.assertEquals(CodegenHelper.toClass(Type.getType(x)), x);
    }
}
