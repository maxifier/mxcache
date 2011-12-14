package com.maxifier.mxcache.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 12:52:20
 * <p>
 * Annotate parameter of cached method to ignore it (i.e. the cache will return the same result if other parameters are
 * equal, but ignored are different).
 * <p>
 * Ignoring arguments of cached methods is error-prone. <b>Be carefull while using this annotation!</b>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Deprecated
public @interface Ignore {
}
