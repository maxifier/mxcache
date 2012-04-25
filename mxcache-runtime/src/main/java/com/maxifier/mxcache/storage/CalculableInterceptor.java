package com.maxifier.mxcache.storage;

import com.maxifier.mxcache.caches.Calculable;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 25.04.12
 * Time: 13:51
 */
public interface CalculableInterceptor {
    Calculable createInterceptedCalculable(Calculable calculable);
}
