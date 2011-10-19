package com.maxifier.mxcache.instrumentation;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 14:58:05
 */
public interface TestProxied extends Serializable {
    String test();

    String test(String in);

    String testInvalid();

    String test(String in, String other);

    void setPrefix(String prefix);

    String justCached(String in);

    String cachedAndProxied(String in);

    String transform(String in);

    String transformWithInstance(String in);
}
