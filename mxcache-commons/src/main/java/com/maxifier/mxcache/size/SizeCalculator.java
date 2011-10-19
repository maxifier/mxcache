package com.maxifier.mxcache.size;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.02.2009
 * Time: 16:23:33
 */
public interface SizeCalculator<T> {
    int getApproximateSize(@NotNull T o, @NotNull SizeIterator iterator);
}
