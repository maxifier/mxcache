/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import java.lang.annotation.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Target(ElementType.METHOD)
@Retention (RetentionPolicy.CLASS)
public @interface ResourceWriter {
    /** @return cписок имен ресурсов */
    String[] value();
}