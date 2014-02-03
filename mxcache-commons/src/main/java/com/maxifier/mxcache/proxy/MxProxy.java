package com.maxifier.mxcache.proxy;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.06.2009
 * Time: 10:12:17
 */
public abstract class MxProxy<T, C extends Resolvable<T>> {
    @Nonnull
    public abstract C getValue();
}
