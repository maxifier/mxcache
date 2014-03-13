/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import javax.annotation.Nonnull;

import java.lang.annotation.Annotation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TransformGeneratorFactory {
    TransformGenerator forMethod(java.lang.reflect.Method method) throws InvalidTransformAnnotations;

    @Nonnull
    TransformGenerator forArgument(Annotation[] annotations, Class paramType) throws InvalidTransformAnnotations;

    TransformGenerator getTransformator(Class param, @Nonnull Transform key);

    TransformGenerator getTransformator(Class<?> paramType, Class owner, String name);
}
