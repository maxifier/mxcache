package com.maxifier.mxcache.impl.caches.def;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.11.2010
 * Time: 8:58:42
 */
public final class TroveHelper {
    public static final Object NULL_REPLACEMENT = new Object() {
        @Override
        public String toString() {
            return "<NULL>";
        }
    };

    private TroveHelper() {}

    public static <T> T unboxNull(T res) {
        //noinspection unchecked
        return res == NULL_REPLACEMENT ? null : res;
    }

    public static <T> T boxNull(T res) {
        //noinspection unchecked
        return res == null ? (T) NULL_REPLACEMENT : res;
    }
}
