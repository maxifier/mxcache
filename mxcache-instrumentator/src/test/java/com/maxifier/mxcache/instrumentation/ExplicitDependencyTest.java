/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.ResourceModificationException;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.instrumentClass;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class ExplicitDependencyTest {
    public void test() throws Exception {
        TestClass t = (TestClass) instrumentClass(TestClassImpl.class).newInstance();

        assert t.test(0) == 0;
        assert t.test(0) == 0;

        MxResource r = MxResourceFactory.getResource("testResource");
        r.writeStart();
        try {
            assert r.isWriting();

            // it should be possible to query cached values
            // (but not evaluate new values)
            assert t.test(0) == 0;
        } finally {
            r.writeEnd();
        }

        assert t.test(0) == 1;
        assert t.test(0) == 1;
    }

    public void testReadWhileWriting() throws Exception {
        TestClass t = (TestClass) instrumentClass(TestClassImpl.class).newInstance();

        Assert.assertEquals(t.test(0), 0);
        Assert.assertEquals(t.test(0), 0);

        MxResource r = MxResourceFactory.getResource("testResource");
        TestThread thread = new TestThread(t);
        r.writeStart();
        try {
            Assert.assertTrue(r.isWriting());
            thread.start();
            thread.join(100);
            Assert.assertTrue(thread.isAlive());
        } finally {
            r.writeEnd();
        }

        thread.join(10);
        thread.check();

        Assert.assertEquals(t.test(0), 2);
        Assert.assertEquals(t.test(0), 2);
    }

    @Test(expectedExceptions = ResourceModificationException.class)
    public void writeFromCachedMethod() throws Exception {
        TestClass t = (TestClass) instrumentClass(TestClassImpl.class).newInstance();
        t.tryWriting();
    }

    private static class TestThread extends Thread {
        private final TestClass t;
        private boolean ok;

        public TestThread(TestClass t) {
            super("TestThread");
            this.t = t;
        }

        @Override
        public void run() {
            // it should wait until resource is cleaned
            ok = t.test(1) == 2;
        }

        public void check() {
            Assert.assertTrue(ok);
        }
    }
}
