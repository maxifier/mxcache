package com.maxifier.mxcache.proxy;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.01.2009
 * Time: 17:35:12
 */
@Test
public class MxProxyGeneratorUTest {
    public interface X {
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

    public static class Y implements X, Serializable {
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
    }


    public void test() throws Exception {
        MxProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getProxyFactory(X.class, Z.class);
        Assert.assertEquals(proxyFactory.createProxy(new Z(new Y("123"))).get(321), "123321");
    }

    @Test
    public void testEquals() throws Exception {
        MxProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getProxyFactory(X.class, Z.class);
        Y y = new Y("123");
        X proxy1 = proxyFactory.createProxy(new Z(y));
        X proxy2 = proxyFactory.createProxy(new Z(y));
        Assert.assertEquals(proxy1, proxy2);
    }

    @Test
    public void testToString() throws Exception {
        MxProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getProxyFactory(X.class, Z.class);
        Y y = new Y("123");
        X proxy = proxyFactory.createProxy(new Z(y));
        Assert.assertTrue(proxy.toString().contains(y.toString()));
    }

    @Test
    public void testSerialize() throws Exception {
        MxProxyFactory<X, Z> proxyFactory = MxProxyGenerator.getProxyFactory(X.class, Z.class);

        X proxy = proxyFactory.createProxy(new Z(new Y("123")));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(bos);

        try {
            oos.writeObject(proxy);
        } finally {
            oos.close();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());


        X newProxy;
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            newProxy = (X) ois.readObject();
        } finally {
            ois.close();
            bis.close();
            bos.close();
        }

        Assert.assertEquals(proxy.get(321), "123321");
        Assert.assertEquals(newProxy.get(321), "123321");
    }
}
