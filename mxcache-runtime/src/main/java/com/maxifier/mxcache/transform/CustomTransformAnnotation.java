package com.maxifier.mxcache.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 12:24:59
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTransformAnnotation {
    Class<? extends TransformFactory> transformGenerator();
}
