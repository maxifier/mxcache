/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import java.io.IOException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TestSerializationClass {
    int cached();

    TestSerializationClass writeAndRead() throws IOException, ClassNotFoundException;

    TestSerializationClass read(byte[] buf) throws IOException, ClassNotFoundException;

    byte[] write() throws IOException;

    String getS();
}
