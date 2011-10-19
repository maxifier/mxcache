package com.maxifier.mxcache.proxy;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.06.2009
 * Time: 10:12:17
 */
public abstract class MxProxy<T, C extends Resolvable<T>> {
    @NotNull
    public abstract C getValue();
}
