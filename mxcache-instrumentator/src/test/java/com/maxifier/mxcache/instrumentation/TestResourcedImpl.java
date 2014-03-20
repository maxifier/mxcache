/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.resource.ResourceReader;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestResourcedImpl implements TestResourced {
    @ResourceReader("testResource")
    @Override
    public void doWithRead(Runnable r) {
        r.run();
    }
}
