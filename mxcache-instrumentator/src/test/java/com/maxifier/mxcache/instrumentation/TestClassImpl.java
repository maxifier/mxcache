/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.resource.ResourceDependency;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestClassImpl implements TestClass {
    int i;

    @ResourceWriter("testResource")
    public void writeResource() {
        // do nothing
    }

    @ResourceReader("testResource")
    private int next() {
        return i++;
    }

    @ResourceDependency("testResource")
    @Cached
    public int test(int a) {
        return next() + a;
    }

    @Cached
    public int tryWriting() {
        writeResource();
        return 77;
    }
}
