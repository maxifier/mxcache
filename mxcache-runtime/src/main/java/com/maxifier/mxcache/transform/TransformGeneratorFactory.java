package com.maxifier.mxcache.transform;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.10.2010
 * Time: 14:29:00
 */
public interface TransformGeneratorFactory {
    TransformGenerator forMethod(java.lang.reflect.Method method) throws InvalidTransformAnnotations;

    @NotNull
    TransformGenerator forArgument(Annotation[] annotations, Class paramType) throws InvalidTransformAnnotations;

    TransformGenerator getTransformator(Class param, @NotNull Transform key);

    TransformGenerator getTransformator(Class<?> paramType, Class owner, String name);
}
