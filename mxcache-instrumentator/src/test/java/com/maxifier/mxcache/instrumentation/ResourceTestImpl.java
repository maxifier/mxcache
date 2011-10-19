package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 9:12:11
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
