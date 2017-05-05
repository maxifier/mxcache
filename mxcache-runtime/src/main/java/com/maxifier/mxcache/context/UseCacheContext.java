/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * This annotation is used in instrumentation. It should be used with constructor arguments of your classes that
 * have @Cached methods.
 * </p><p>
 * It tells MxCache to use specified cache context that is passed via argument.
 * The annotated parameters type should extend {@link com.maxifier.mxcache.context.CacheContext} interface,
 * otherwise it will fail in runtime.
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UseCacheContext {
}
