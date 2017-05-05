/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.exam;

import com.maxifier.mxcache.stubgen.lib.InterfaceA;
import com.maxifier.mxcache.stubgen.lib.InterfaceZ;
import com.maxifier.mxcache.stubgen.lib.RecursiveGeneric;

import java.util.List;

/**
 * Test1
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-07 11:30)
 */
public class TestA implements InterfaceA<StringBuilder> {
    @Override
    public StringBuilder get() {
        return null;
    }

    @Override
    public <R extends Integer, X extends Throwable & Runnable> R genericMethod(R input) {
        return null;
    }

    @Override
    public List<? super StringBuilder> getList() {
        return null;
    }

    public void test() {
        InterfaceZ z = null;

        z.getX();
    }

    public void testRecursiveGeneric() {
        new RecursiveGeneric().get().complexMethod();
    }
}
