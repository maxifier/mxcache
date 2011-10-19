package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.resource.ResourceDependency;
import com.maxifier.mxcache.resource.ResourceReader;
import com.maxifier.mxcache.resource.ResourceWriter;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 12:45:01
 */
public class TestClassImpl implements TestClass {
    int i;

    @ResourceWriter("testResource")
    public void writeResource() {
        // do nothing
    }

    @ResourceReader("testResource")
    private int next() {
        return i++;
    }

    @ResourceDependency("testResource")
    @Cached
    public int test(int a) {
        return next() + a;
    }

    @Cached
    public int tryWriting() {
        writeResource();
        return 77;
    }
}
