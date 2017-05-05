/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import org.testng.annotations.Test;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.instrumentClass;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class SerializationFTest {

    public void testSerialVersionUID() throws Exception {
        TestSerializationClass t = (TestSerializationClass) instrumentClass(TestSerializationClassImpl.class).newInstance();
        TestSerializationClass o = new TestSerializationClassImpl("745");
        assert o.read(t.write()).getS().equals("123");
        assert t.read(t.write()).getS().equals("123");
        assert o.read(o.write()).getS().equals("745");
        assert t.read(o.write()).getS().equals("745");
    }

    public void testCached() throws Exception {
        TestSerializationClass t = (TestSerializationClass) instrumentClass(TestSerializationClassImpl.class).newInstance();
        assert t.cached() == 0;
        assert t.cached() == 0;
        TestSerializationClass o = t.writeAndRead();
        assert o.cached() == 1;
        assert o.cached() == 1;
    }

    public void testCachedWithReadObject() throws Exception {
        TestSerializationClass t = (TestSerializationClass) instrumentClass(TestSerializationClassImplWithReadObject.class).newInstance();
        assert t.cached() == 0;
        assert t.cached() == 0;
        TestSerializationClass o = t.writeAndRead();
        assert o.cached() == 1;
        assert o.cached() == 1;
    }
}
