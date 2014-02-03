package com.maxifier.mxcache.impl.instanceprovider;

import javax.annotation.Nonnull;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 15.09.2010
* Time: 9:12:01
*/
public interface Binder<T> {
    void toProvider(@Nonnull Provider<T> provider);

    void toInstance(@Nonnull T instance);

    void toClass(@Nonnull Class<? extends T> cls);
}
