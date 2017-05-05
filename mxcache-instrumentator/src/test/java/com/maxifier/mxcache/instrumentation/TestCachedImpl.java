/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.UseCacheContext;
import com.maxifier.mxcache.hashing.CharArrayHashingStrategy;
import com.maxifier.mxcache.hashing.HashingStrategy;
import com.maxifier.mxcache.hashing.IdentityHashing;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;
import com.maxifier.mxcache.resource.TrackDependency;
import com.maxifier.mxcache.transform.Ignore;
import com.maxifier.mxcache.transform.Transform;
import com.maxifier.mxcache.transform.WeakKey;
import gnu.trove.strategy.IdentityHashingStrategy;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class TestCachedImpl implements TestCached, Serializable {
    private int i;
    private String s;
    private int x;

    public static String test;

    @SuppressWarnings( { "UnusedDeclaration" })
    // used via reflection
    public TestCachedImpl() {
        this("");
    }
    
    public TestCachedImpl(String s) {
        this.s = s;
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
    @TrackDependency(DependencyTracking.INSTANCE)
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

    @Cached
    public String getByArray(int[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(long[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(short[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(double[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(float[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(byte[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(boolean[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(char[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(Object[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(long a, int[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(int a, long[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(String a, short[] array) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArray(Object[] array1, double[] array2) {
        return Arrays.toString(array1) + x++ + Arrays.toString(array2);
    }
    @Cached
    public String getByArray(float[] array, int a) {
        return a + Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArrayIdentityStr(byte[] array, @IdentityHashing String custom) {
        return custom + Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArrayIdentity(@IdentityHashing boolean[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArraySameStrategy(@HashingStrategy(CharArrayHashingStrategy.class) char[] array) {
        return Arrays.toString(array) + x++;
    }
    @Cached
    public String getByArrayIdentity2(Object[] array1, @IdentityHashing Object[] array2) {
        return Arrays.toString(array1) + x++ + Arrays.toString(array2);
    }
    @Cached
    public String getSingleByIdentity(@IdentityHashing String s) {
        return s + x++;
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

    @Cached
    @Override
    public String ignore(@Ignore String x, long y) {
        return x + y;
    }

    @Cached
    @Override
    public String ignore(@Ignore long x, @WeakKey String y) {
        return x + y;
    }

    @Cached
    @Override
    public String ignore(String x, @Ignore long y, @WeakKey String z) {
        return x + y + z;
    }

    @Override
    public String exceptionTest() throws IOException {
        throw new IOException("This exception should be passed to test method");
    }

    @Override
    @ResourceReader("#123")
    public void readResource() {
        System.out.println("Some code");
        // nothing
    }

    @Override
    @ResourceReader("test")
    public void readResourceWithException(Runnable r) {
        r.run();
        throw new IllegalStateException("123");
    }

    @Override
    @ResourceWriter("#123")
    public void writeResource() {
        // nothing
    }

    @Override
    @ResourceReader("123")
    public void readStatic() {
        // nothing
    }

    @Override
    @ResourceWriter("123")
    public void writeStatic() {
        // nothing
    }
    
    public void setS(String s) {
        this.s = s;
    }
}
