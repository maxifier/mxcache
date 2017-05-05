/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;

/**
* @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class MxGenericProxyGeneratorUTest {
    public interface X {
        String get(long t);
    }

    public interface XChild extends X {
        String get(String s);

        @Override
        String get(long t);
    }

    public static class Z implements Resolvable<X>, Serializable {
        private Y y;

        public Z() {
        }

        public Z(Y y) {
            this.y = y;
        }

        @Override
        public Y getValue() {
            return y;
        }
    }

    public static class Y implements X, XChild, Serializable {
        private String v;

        public Y() {
        }

        public Y(String v) {
            this.v = v;
        }

        @Override
        public String get(long t) {
            return v + t;
        }

        @Override
        public String get(String s) {
            return v + s;
        }
    }


    public void test() throws Exception {
        final MxGenericProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getGenericProxyFactory(X.class, Z.class);
        final XChild proxy = (XChild) proxyFactory.createProxy(Y.class, new Z(new Y("123")));
        Assert.assertEquals(proxy.get(321), "123321");
        Assert.assertEquals(proxy.get("mama"), "123mama");
    }

    @Test
    public void testEquals() throws Exception {
        final MxGenericProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getGenericProxyFactory(X.class, Z.class);
        final Y y = new Y("123");
        final XChild proxy1 = (XChild) proxyFactory.createProxy(Y.class, new Z(y));
        final XChild proxy2 = (XChild) proxyFactory.createProxy(Y.class, new Z(y));
        Assert.assertEquals(proxy1, proxy2);
    }

    @Test
    public void testToString() throws Exception {
        final MxGenericProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getGenericProxyFactory(X.class, Z.class);
        final Y y = new Y("123");
        final XChild proxy = (XChild) proxyFactory.createProxy(Y.class, new Z(y));
        Assert.assertTrue(proxy.toString().contains(y.toString()));
    }

    @Test
    public void testSerialize() throws Exception {
        final MxGenericProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getGenericProxyFactory(X.class, Z.class);

        final XChild proxy = (XChild) proxyFactory.createProxy(Y.class, new Z(new Y("123")));

        final XChild newProxy = serialize(proxy);

        Assert.assertEquals(proxy.get(321), "123321");
        Assert.assertEquals(newProxy.get(321), "123321");

        Assert.assertEquals(proxy.get("mama"), "123mama");
        Assert.assertEquals(newProxy.get("mama"), "123mama");
    }

    private static <T> T serialize(T proxy) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(proxy);
        } finally {
            oos.close();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());

        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            //noinspection unchecked
            return (T) ois.readObject();
        } finally {
            ois.close();
            bis.close();
            bos.close();
        }
    }
}