/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.PublicAPI;

import java.lang.annotation.*;

/**
 * UseEhcache - this annotation makes MxCache use Ehcache strategy for methods annotated with it.
 * Don't forget to put @Cached annotation among with it.
 * Name for cache is taken from @Cached annotation 'name' attribute.
 * By default the configuration of Ehcache is read from ehcache.xml from META-INF, though you can override it
 * with attribute of this annotation.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseEhcache {
    /**
     * @return ehcache XML config URL, use "classpath://" to refer to classpath resources (the classloader of cache
     *    owner class is used to obtain configuration in this case).
     */
    @PublicAPI
    String configURL() default "classpath://META-INF/ehcache.xml";
}
