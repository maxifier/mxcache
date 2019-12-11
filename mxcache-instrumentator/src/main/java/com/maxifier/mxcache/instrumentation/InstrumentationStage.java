/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.ClassVisitor;

import java.util.List;

/**
 * Instrumentation stage should also extend ClassVisitor, but it's impossible due to changes in ASM API:
 * ClassVisitor is not an interface anymore, it is just a class.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface InstrumentationStage /* extends ClassVisitor */ {
    boolean isClassChanged();

    ClassVisitor asClassVisitor();

    ClassVisitor getDetector();

    List<ClassDefinition> getAdditionalClasses();
}
