package com.maxifier.mxcache.instrumentation;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 25.10.2010
* Time: 10:53:42
*/
public class TestTransforms {
    private final String suffix;

    public TestTransforms(String suffix) {
        this.suffix = suffix;
    }

    public String t(String in) {
        return in + suffix;
    }

    public String n(String in) {
        return in;
    }
}
