/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
    @Nonnull
    <T> T forClass(@Nonnull Class<T> cls);
}
