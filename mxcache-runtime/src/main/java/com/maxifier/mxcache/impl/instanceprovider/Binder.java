package com.maxifier.mxcache.impl.instanceprovider;

import org.jetbrains.annotations.NotNull;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 15.09.2010
* Time: 9:12:01
*/
public interface Binder<T> {
    void toProvider(@NotNull Provider<T> provider);

    void toInstance(@NotNull T instance);

    void toClass(@NotNull Class<? extends T> cls);
}
