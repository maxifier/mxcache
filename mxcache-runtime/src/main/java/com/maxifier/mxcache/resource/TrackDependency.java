package com.maxifier.mxcache.resource;

import com.maxifier.mxcache.DependencyTracking;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.04.2010
 * Time: 13:21:45
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface TrackDependency {
    DependencyTracking value() default DependencyTracking.STATIC;
}
