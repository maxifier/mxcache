/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

import java.io.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TestSerializationClassImpl implements Serializable, TestSerializationClass {
    TestSerializationClassImpl(String s) {
        this.s = s;
    }

    public TestSerializationClassImpl() {
        s = "123";
    }

    int i;

    String s;

    @Cached
    public int cached() {
        return i++;
    }

    public String getS() {
        return s;
    }

    public TestSerializationClassImpl writeAndRead() throws IOException, ClassNotFoundException {
        return read(write());
    }

    public TestSerializationClassImpl read(byte[] buf) throws IOException, ClassNotFoundException {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
        //noinspection unchecked
        return (TestSerializationClassImpl) ois.readObject();
    }

    public byte[] write() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(this);
        byte[] buf = bos.toByteArray();
        return buf;
    }
}
