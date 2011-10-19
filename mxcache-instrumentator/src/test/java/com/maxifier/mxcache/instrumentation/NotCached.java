package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 11:56:22
 */
public class NotCached {
    public void cc(Cached c) {
        c.group();
    }
}
