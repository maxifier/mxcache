package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.CacheContextImpl;
import com.maxifier.mxcache.context.UseCacheContext;
import com.maxifier.mxcache.transform.Ignore;
import com.maxifier.mxcache.transform.Transform;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by IntelliJ IDEA.
* User: dalex
* Date: 24.03.2010
* Time: 11:20:16
*/
public class TestCachedImpl implements TestCached {
    private int i;
    private String s = "";
    private int x;

    public static String test;

    @SuppressWarnings( { "UnusedDeclaration" })
    // used via reflection
    public TestCachedImpl() {
    }

    @SuppressWarnings( { "UnusedDeclaration" })
    // used via reflection
    public TestCachedImpl(@UseCacheContext CacheContext context) {
    }

    static {
        test = "TE" + "ST";
    }

    public void reset() {
        this.i = 0;
    }

    @Cached (group = "g")
    public int get() {
        return i++;
    }

    @Cached(tags = "t")
    public int get(int a) {
        return a + i++;
    }

    @CacheCleaningAnnotation
    @Cached(tags = {"t1", "t2"})
    public int get(int a, int b) {
        return a + b + i++;
    }

    @Cached
    public String getString() {
        s += "a";
        return s;
    }

    private static int j;

    @Cached(tags = "t", group = "g")
    public static int getStatic() {
        return j++;
    }

    @Cached
    public String test(String a, String b) {
        return a + b + x++;
    }


    @Transform(owner = TestCachedImpl.class, method = "threeLetters")
    @Retention(RetentionPolicy.RUNTIME)
    @interface ThreeLetters {}

    @SuppressWarnings("UnusedDeclaration")
    public static String threeLetters(Object o) {
        return o == null ? "" : o.toString().substring(0, 3);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static long div2(long a) {
        return a / 2;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static String toConstString(long a) {
        return "test";
    }

    @Cached
    public String transform(@ThreeLetters Long v) {
        return "(Long)" + v.toString();
    }

    @Cached
    public String transform(@ThreeLetters String s) {
        return "(String)" + s;
    }

    @Cached
    public String transformPrimitive(@Transform(owner = TestCachedImpl.class, method = "div2") long v) {
        return Long.toString(v);
    }

    @Cached
    public String transformPrimitiveToString(@Transform(owner = TestCachedImpl.class, method = "toConstString") long v) {
        return Long.toString(v);
    }

    @Cached
    @Strategy(TestStrategy.class)
    @Override
    public String nullCache(String s) {
        return s + x++;
    }

    @Cached
    @Override
    public String ignore(String x, @Ignore String y) {
        return x + y;
    }

    @Cached
    @Override
    public String ignore(@Ignore String x) {
        return x;
    }

    @Override
    public String exceptionTest() throws IOException {
        throw new IOException("This exception should be passed to test method");
    }

    public TestCached reloadWithContext(CacheContextImpl context) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(this);
        } finally {
            oos.close();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        TestMxObjectInput testMxObjectInput = new TestMxObjectInput(bis, context, getClass().getClassLoader());
        return (TestCached) testMxObjectInput.readObject();
    }

}
