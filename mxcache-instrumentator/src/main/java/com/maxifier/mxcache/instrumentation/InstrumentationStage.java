/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.ClassVisitor;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface InstrumentationStage extends ClassVisitor {
    boolean isClassChanged();

    ClassVisitor getDetector();

    List<ClassDefinition> getAdditionalClasses();
}
