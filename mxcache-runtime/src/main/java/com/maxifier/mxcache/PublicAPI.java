/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * This is just a marker annotation that says that this method should not be deleted or modified because it is
 * a part of public API of MxCache.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Retention(value = RetentionPolicy.SOURCE)
@Target(value = {METHOD, TYPE, CONSTRUCTOR, FIELD, PARAMETER})
@Documented
public @interface PublicAPI {
}
