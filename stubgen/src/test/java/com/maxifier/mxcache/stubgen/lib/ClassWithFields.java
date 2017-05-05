/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

/**
 * ClassWithStaticField
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 10:57)
 */
public class ClassWithFields {
    // we need non-constant because we don't want it to be inlined by compiler
    public static final int A = getA();
    public static final boolean B = getB();
    public static final Runnable UNUSED = getUnused();

    public final String c = getC();

    private static int getA() {
        return 42;
    }

    private static boolean getB() {
        return true;
    }

    private String getC() {
        return "123";
    }

    private static Runnable getUnused() {
        return null;
    }
}
