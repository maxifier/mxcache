package com.maxifier.mxcache.size;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 23.03.2009
 * Time: 11:20:20
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */

@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
public @interface EmptySize {
}
