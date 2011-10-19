package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 12:57:50
 */
public class CachedNative {
    @Cached
    public native int thisMethodShouldFailToInstrument();
}
