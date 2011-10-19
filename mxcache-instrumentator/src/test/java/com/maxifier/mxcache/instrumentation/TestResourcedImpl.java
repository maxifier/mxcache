package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.resource.ResourceReader;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.04.11
 * Time: 10:25
 */
public class TestResourcedImpl implements TestResourced {
    @ResourceReader("testResource")
    @Override
    public void doWithRead(Runnable r) {
        r.run();
    }
}
