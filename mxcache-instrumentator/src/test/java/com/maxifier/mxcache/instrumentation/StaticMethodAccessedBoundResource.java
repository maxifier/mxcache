/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.resource.ResourceReader;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StaticMethodAccessedBoundResource {

    @ResourceReader("#123")
    public static void x() {

    }
}
