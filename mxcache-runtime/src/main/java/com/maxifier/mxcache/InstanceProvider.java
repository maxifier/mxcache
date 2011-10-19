package com.maxifier.mxcache;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.10.2010
 * Time: 11:12:36
 */
public interface InstanceProvider {
    /**
     *
     * @param cls class
     * @param <T> type
     * @return instance of class; may return different instances every call.
     * @throws NoSuchInstanceException if there were problems accessing instance (e.g. class has no corresponding
     * constructor).
     */
    @NotNull <T> T forClass(@NotNull Class<T> cls);
}
