package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.resource.ResourceReader;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 29.02.12
 * Time: 22:45
 */
public class StaticMethodAccessedBoundResource {

    @ResourceReader("#123")
    public static void x() {

    }
}
