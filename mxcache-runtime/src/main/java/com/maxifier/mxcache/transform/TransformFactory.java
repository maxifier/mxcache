package com.maxifier.mxcache.transform;

import java.lang.annotation.Annotation;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 12:27:27
 */
public interface TransformFactory<A extends Annotation> {
    TransformGenerator create(A annotation, Annotation[] allAnnotations, Class paramType);
}
