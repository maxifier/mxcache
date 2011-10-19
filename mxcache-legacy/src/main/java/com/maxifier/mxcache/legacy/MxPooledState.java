package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.proxy.Resolvable;
import com.maxifier.mxcache.legacy.converters.MxConvertState;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.02.2009
 * Time: 14:34:17
 */
public final class MxPooledState<T> implements MxConvertState<T> {
    public static final MxPooledState<Object> READY = create(0, "READY", 1.0, 1.0);
    public static final MxPooledState<byte[]> BYTES = create(2, "BYTES", 1.5, 0.7);
    public static final MxPooledState<MxResource> ONDISK = create(3, "ONDISK", 1.5, 0.01);
    public static final MxPooledState<Resolvable<Object>> PARAMETRIC = MxPooledState.create(1, "PARAMETRIC", 1.0, 0.0);

    private final String name;
    private final int id;
    private final double preferrenceFactor;
    private final double size;

    private static <T> MxPooledState<T> create(int id, String name, double preferrence, double size) {
        return new MxPooledState<T>(id, name, preferrence, size);
    }

    private MxPooledState(int id, String name, double preferrenceFactor, double size) {
        this.id = id;
        this.name = name;
        this.preferrenceFactor = preferrenceFactor;
        this.size = size;
    }

    @Override
    public int getId() {
        return id;
    }

    public String toString() {
        return name;
    }

    public double getPreferrenceFactor() {
        return preferrenceFactor;
    }

    public double getSize() {
        return size;
    }
}
