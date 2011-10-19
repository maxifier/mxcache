package com.maxifier.mxcache;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 07.09.2010
 * Time: 8:22:29
 */
public class NonInstrumentedCacheException extends MxCacheException {
    public NonInstrumentedCacheException(Method m) {
        super(m.toString());
    }
}
