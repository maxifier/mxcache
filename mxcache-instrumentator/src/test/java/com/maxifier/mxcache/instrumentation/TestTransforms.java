/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class TestTransforms {
    private final String suffix;

    public TestTransforms(String suffix) {
        this.suffix = suffix;
    }

    public String t(String in) {
        return in + suffix;
    }

    public String n(String in) {
        return in;
    }
}
