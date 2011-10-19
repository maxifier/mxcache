package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

import static com.maxifier.mxcache.instrumentation.InstrumentationTestHelper.instrumentClass;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 11:51:16
 */
public class CachedVoid {
    @Cached
    public void thisMethodShouldFailToInstrument() {
        
    }
}
