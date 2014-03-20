/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p>
 * Annotate parameter of cached method to ignore it (i.e. the cache will return the same result if other parameters are
 * equal, but ignored are different).</p>
 * <p>
 * Ignoring arguments of cached methods is error-prone. <b>Be carefull while using this annotation!</b>
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Deprecated
public @interface Ignore {
}
