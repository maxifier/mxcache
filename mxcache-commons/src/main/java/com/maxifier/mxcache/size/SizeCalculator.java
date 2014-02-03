package com.maxifier.mxcache.size;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.02.2009
 * Time: 16:23:33
 */
public interface SizeCalculator<T> {
    int getApproximateSize(@Nonnull T o, @Nonnull SizeIterator iterator);
}
