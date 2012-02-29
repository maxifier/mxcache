package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.context.CacheContextImpl;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 11:22:40
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

    TestCached reloadWithContext(CacheContextImpl context) throws IOException, ClassNotFoundException;

    void readResource();

    void writeResource();

    void readStatic();

    void writeStatic();
}
