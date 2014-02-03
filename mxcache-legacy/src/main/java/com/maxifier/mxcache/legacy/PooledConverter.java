package com.maxifier.mxcache.legacy;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.02.11
 * Time: 17:08
 */
public interface PooledConverter<T> {
    @Nonnull
    MxResource save(byte[] bytes, T value);

    @Nonnull
    T load(MxResource resource);

    @Nonnull
    T deserialize(byte[] bytes);

    byte[] serialize(T value);

    double storageCost(MxPooledState<?> state, boolean needsForward, float usageForecast);

    void reportCost(MxPooledState<?> from, MxPooledState<?> to, long time);
}
