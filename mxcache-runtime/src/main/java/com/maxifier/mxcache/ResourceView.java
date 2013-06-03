/* Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 * Maxifier Ltd  proprietary and confidential.
 * Use is subject to license terms.
 */
package com.maxifier.mxcache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (28.05.13)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ResourceView {
}
