/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TestProxied extends Serializable {
    String test();

    String test(String in);

    String testInvalid();

    String test(String in, String other);

    void setPrefix(String prefix);

    String justCached(String in);

    String cachedAndProxied(String in);

    String transform(String in);

    String transformWithInstance(String in);
}
