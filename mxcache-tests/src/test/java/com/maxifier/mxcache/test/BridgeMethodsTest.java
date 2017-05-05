/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.test;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;
import org.testng.annotations.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertEqualsNoOrder;
import static org.testng.Assert.assertTrue;

/**
 * BridgeMethodsFTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-28 11:01)
 */
@Test
public class BridgeMethodsTest {
    public void testThereIsABridgeMethod() throws NoSuchMethodException {
        assertTrue(Y.class.getDeclaredMethod("getSomething", Object.class).isBridge());

        assertTrue(S.class.getDeclaredMethod("doWrite", Object.class).isBridge());
    }

    public void testResourceOperationsDirectly() throws Exception {
        S s = new S();

        s.doWrite("test");
        assertEquals(s.doRead(), "test");
    }

    public void testResourceOperationsWithBridge() throws Exception {
        R<String> s = new S();

        s.doWrite("test");
        assertEquals(s.doRead(), "test");
    }

    public void testNoDoubleInstrumented() {
        List<String> fieldNames = new ArrayList<String>();
        for (Field field : Y.class.getDeclaredFields()) {
            fieldNames.add(field.getName());
        }
        assertEqualsNoOrder(fieldNames.toArray(), new String[] {"v", "serialVersionUID", "getSomething$cache$0"});
    }

    // getSomething(String) is invoked here
    public void testCacheWorksDirectly() {
        Y y = new Y();
        assertEquals(y.getSomething("34"), 0);
        assertEquals(y.getSomething("34"), 0);

        MxCache.getCleaner().clearCacheByInstance(y);

        assertEquals(y.getSomething("34"), 1);
    }

    // bridge method getSomething(Object) is invoked here
    public void testCacheWorksWithBridge() {
        X<String> y = new Y();
        assertEquals(y.getSomething("34"), 0);
        assertEquals(y.getSomething("34"), 0);

        MxCache.getCleaner().clearCacheByInstance(y);

        assertEquals(y.getSomething("34"), 1);
    }

    static class X<T> {
        int getSomething(T t) {
            return t.hashCode();
        }
    }

    static class Y extends X<String> {
        int v;

        @Override
        @Cached
        int getSomething(String s) {
            return v++;
        }
    }

    static class R<T> {
        T doRead() throws Exception {
            return null;
        }

        void doWrite(T t) throws Exception {
            // do nothing
        }
    }

    static class S extends R<String> {
        String s;

        @Override
        @ResourceReader("test")
        String doRead() throws Exception {
            // it's a bit hacky, but we want to ensure that the resource is really locked exactly one time
            MxResource v = MxResourceFactory.getResource("test");
            Field lockField = v.getClass().getSuperclass().getDeclaredField("lock");
            lockField.setAccessible(true);
            ReentrantReadWriteLock lock = (ReentrantReadWriteLock)lockField.get(v);
            assertEquals(lock.getReadHoldCount(), 1);

            return s;
        }

        @Override
        @ResourceWriter("test")
        void doWrite(String s) throws Exception {
            // it's a bit hacky, but we want to ensure that the resource is really locked exactly one time
            MxResource v = MxResourceFactory.getResource("test");
            Field lockField = v.getClass().getSuperclass().getDeclaredField("lock");
            lockField.setAccessible(true);
            ReentrantReadWriteLock lock = (ReentrantReadWriteLock)lockField.get(v);
            assertEquals(lock.getWriteHoldCount(), 1);

            this.s = s;
        }
    }
}
