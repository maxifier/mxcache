/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import java.lang.annotation.Annotation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface TransformFactory<A extends Annotation> {
    TransformGenerator create(A annotation, Annotation[] allAnnotations, Class paramType);
}
