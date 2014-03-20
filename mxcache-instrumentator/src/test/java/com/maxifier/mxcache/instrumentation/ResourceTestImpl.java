/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceTestImpl implements ResourceTest {
    @Override
    @ResourceWriter("testResource")
    public void writeIt() {
        assert MxResourceFactory.getResource("testResource").isWriting();
    }

    @Override
    @ResourceReader({ "testResource1", "testResource2" })
    public void readIt() {
        assert MxResourceFactory.getResource("testResource1").isReading();
        assert MxResourceFactory.getResource("testResource2").isReading();
    }

    @Override
    @ResourceWriter({ "testResource1", "testResource2" })
    public void writeMultiple() {
        assert MxResourceFactory.getResource("testResource1").isWriting();
        assert MxResourceFactory.getResource("testResource2").isWriting();
    }
}
