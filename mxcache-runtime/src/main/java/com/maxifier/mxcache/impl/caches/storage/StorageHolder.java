package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.storage.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 02.11.2010
 * Time: 12:17:07
 */
public interface StorageHolder<T extends Storage> {
    void setStorage(@NotNull T t);
}
