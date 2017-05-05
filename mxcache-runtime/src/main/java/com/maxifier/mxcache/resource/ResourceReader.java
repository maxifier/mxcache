/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import java.lang.annotation.*;

/**
 * Use this annotation to mark your method as resource reader explicitly.
 * This is just a shorthand for the following code:
 * <code>
 * MxResource res = MxResourceFactory.getResource("here goes a name of resource");
 * res.readStart();
 * try {
 *     ...your code goes here...
 * } finally {
 *     res.readEnd();
 * }
 * </code>
 *
 * @see com.maxifier.mxcache.resource.MxResource
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Documented
@Target(ElementType.METHOD)
@Retention (RetentionPolicy.CLASS)
public @interface ResourceReader {
    /**
     * @return names of resources
     */
    String[] value();
}
