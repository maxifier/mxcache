/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.context.CacheContextImpl;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TestCached extends Serializable {
    void reset();

    int get();

    int get(int a);

    int get(int a, int b);

    String test(String a, String b);

    String getString();

    String nullCache(String s);

    String ignore(String x, String y);

    String ignore(String x);
    
    String ignore(String x, long y);
    
    String ignore(long x, String y);

    String ignore(String x, long y, String z);

    String exceptionTest() throws IOException;

    String transform(Long v);

    String transform(String s);

    String transformPrimitive(long v);

    String transformPrimitiveToString(long v);

    void readResource();

    void writeResource();

    void readStatic();

    void writeStatic();

    List<String> getBatch(List<String> in);

    String[] getBatch(String... in);

    Map<String, String> getBatch(Set<String> in);

    Map<String, String> getBatchArrayToMap(String... in);

    void setS(String s);
}
