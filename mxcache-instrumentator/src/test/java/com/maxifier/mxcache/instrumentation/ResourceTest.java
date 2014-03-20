/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ResourceTest {
    void writeIt();

    void writeMultiple();

    void readIt();
}
