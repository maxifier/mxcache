/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.UseCacheContext;
import com.maxifier.mxcache.impl.caches.batch.BatchCache;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;
import com.maxifier.mxcache.resource.TrackDependency;
import com.maxifier.mxcache.transform.Ignore;
import com.maxifier.mxcache.transform.Transform;
import com.maxifier.mxcache.transform.WeakKey;
import gnu.trove.THashMap;

import java.io.*;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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


    @Cached
    @BatchCache
    @Override
    public List<String> getBatch(List<String> in) {
        List<String> res = new ArrayList<String>(in.size());
        for (String v : in) {
            res.add(v + s);
        }
        return res;
    }

    @Cached
    @BatchCache
    @Override
    public String[] getBatch(String... in) {
        String[] res = new String[in.length];
        for (int i = 0; i < in.length; i++) {
            res[i] = in[i] + s;
        }
        return res;
    }

    @Cached
    @BatchCache
    @Override
    public Map<String, String> getBatch(Set<String> in) {
        Map<String, String> res = new THashMap<String, String>(in.size());
        for (String v : in) {
            res.put(v, v + s);
        }
        return res;
    }

    @Cached
    @BatchCache
    @Override
    public Map<String, String> getBatchArrayToMap(String... in) {
        Map<String, String> res = new THashMap<String, String>(in.length);
        for (String v : in) {
            res.put(v, v + s);
        }
        return res;
    }
    
    public void setS(String s) {
        this.s = s;
    }
}
