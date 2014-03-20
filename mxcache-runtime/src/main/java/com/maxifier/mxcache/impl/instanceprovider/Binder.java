/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Binder<T> {
    void toProvider(@Nonnull Provider<T> provider);

    void toInstance(@Nonnull T instance);

    void toClass(@Nonnull Class<? extends T> cls);
}
