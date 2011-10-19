package com.maxifier.mxcache.instrumentation;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 11:51:42
 */
public interface TestSerializationClass {
    int cached();

    TestSerializationClass writeAndRead() throws IOException, ClassNotFoundException;

    TestSerializationClass read(byte[] buf) throws IOException, ClassNotFoundException;

    byte[] write() throws IOException;

    String getS();
}
