package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.storage.Storage;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.11.2010
 * Time: 17:28:16
 */
public interface StorageFactory<T> {
    @NotNull
    Storage createStorage(T owner) throws InvocationTargetException, IllegalAccessException, InstantiationException;

    String getImplementationDetails();
}
