package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.proxy.UseProxy;
import com.maxifier.mxcache.transform.ReversibleTransform;
import com.maxifier.mxcache.transform.Transform;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 14:55:45
 */
public class TestProxiedImpl implements TestProxied {
    private String prefix = "";

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    @UseProxy(TestProxyFactory.class)
    public String test() {
        return prefix + "123";
    }

    @Override
    @UseProxy(TestProxyFactoryUninstantiatable.class)
    public String testInvalid() {
        return prefix + "123";
    }

    @Override
    @UseProxy(TestProxyFactory.class)
    public String test(String in) {
        return prefix + in;
    }

    @Override
    @UseProxy(TestProxyFactory.class)
    public String test(String in, String other) {
        return prefix + in + other;
    }

    @UseProxy(TestProxyFactory.class)
    public static String testStatic(String in) {
        return in;
    }

    @Override
    @Cached
    public String justCached(String in) {
        return prefix + in + "C";
    }

    @Override
    @Cached
    @UseProxy(TestProxyFactory.class)
    public String cachedAndProxied(String in) {
        return prefix + in + "C";
    }

    public static String t(String in) {
        return in + "_";
    }

    public static String n(String in) {
        return in;
    }

    @Override
    @UseProxy(TestProxyFactory.class)
    public String transform(@ReversibleTransform(forward = @Transform(owner = TestProxiedImpl.class, method = "t"), backward = @Transform(owner = TestProxiedImpl.class, method = "n")) String in) {
        return prefix + in + "T";
    }

    @Override
    @UseProxy(TestProxyFactory.class)
    public String transformWithInstance(@ReversibleTransform(forward = @Transform(owner = TestTransforms.class, method = "t"), backward = @Transform(owner = TestTransforms.class, method = "n")) String in) {
        return prefix + in + "T";
    }
}
