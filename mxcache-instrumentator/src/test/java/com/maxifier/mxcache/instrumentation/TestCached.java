/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import java.io.IOException;
import java.io.Serializable;

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

    void readResourceWithException(Runnable r);

    void setS(String s);

    String getByArray(int[] array);
    String getByArray(long[] array);
    String getByArray(short[] array);
    String getByArray(double[] array);
    String getByArray(float[] array);
    String getByArray(byte[] array);
    String getByArray(boolean[] array);
    String getByArray(char[] array);
    String getByArray(Object[] array);
    String getByArray(long a, int[] array);
    String getByArray(int a, long[] array);
    String getByArray(String a, short[] array);
    String getByArray(Object[] array1, double[] array2);
    String getByArray(float[] array, int a);
    String getByArrayIdentityStr(byte[] array, String custom);
    String getByArrayIdentity(boolean[] array);
    String getByArraySameStrategy(char[] array);
    String getByArrayIdentity2(Object[] array1, Object[] array2);
    String getSingleByIdentity(String x);
}
