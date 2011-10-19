package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 11:50:01
 */
public abstract class CachedAbstract {
    @Cached
    public abstract int thisMethodShouldFailToInstrument();
}
