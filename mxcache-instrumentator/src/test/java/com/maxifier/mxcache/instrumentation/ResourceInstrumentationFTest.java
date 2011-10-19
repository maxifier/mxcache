package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import org.testng.annotations.Test;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.instrumentClass;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 9:08:05
 */
@Test
public class ResourceInstrumentationFTest {
    private static ResourceTest instrument() throws Exception {
        return (ResourceTest) instrumentClass(ResourceTestImpl.class).newInstance();
    }

    public void testWrite() throws Exception {
        ResourceTest r = instrument();
        assert !MxResourceFactory.getResource("testResource").isWriting();
        r.writeIt();
        assert !MxResourceFactory.getResource("testResource").isWriting();
    }

    public void testWriteMultiple() throws Exception {
        ResourceTest r = instrument();
        assert !MxResourceFactory.getResource("testResource1").isWriting();
        assert !MxResourceFactory.getResource("testResource2").isWriting();
        r.writeMultiple();
        assert !MxResourceFactory.getResource("testResource1").isWriting();
        assert !MxResourceFactory.getResource("testResource2").isWriting();
    }

    public void testRead() throws Exception {
        ResourceTest r = instrument();
        assert !MxResourceFactory.getResource("testResource1").isReading();
        assert !MxResourceFactory.getResource("testResource2").isReading();
        r.readIt();
        assert !MxResourceFactory.getResource("testResource1").isReading();
        assert !MxResourceFactory.getResource("testResource2").isReading();
    }
}
