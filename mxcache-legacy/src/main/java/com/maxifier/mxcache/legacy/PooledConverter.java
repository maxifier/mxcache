package com.maxifier.mxcache.legacy;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.02.11
 * Time: 17:08
 */
public interface PooledConverter<T> {
    @NotNull
    MxResource save(byte[] bytes, T value);

    @NotNull
    T load(MxResource resource);

    @NotNull
    T deserialize(byte[] bytes);

    byte[] serialize(T value);

    double storageCost(MxPooledState<?> state, boolean needsForward, float usageForecast);

    void reportCost(MxPooledState<?> from, MxPooledState<?> to, long time);
}
