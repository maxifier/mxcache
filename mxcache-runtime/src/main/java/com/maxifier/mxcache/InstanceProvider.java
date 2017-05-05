/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import javax.annotation.Nonnull;

/**
 * MxCache uses InstanceProvider in obtain following objects:
 * <ul>
 *     <li>{@link com.maxifier.mxcache.provider.CachingStrategy};</li>
 *     <li>{@link com.maxifier.mxcache.proxy.ProxyFactory};</li>
 *     <li>{@link com.maxifier.mxcache.transform.TransformGenerator};</li>
 *     <li>any other strategy-specific cases.</li>
 * </ul>
 *
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
