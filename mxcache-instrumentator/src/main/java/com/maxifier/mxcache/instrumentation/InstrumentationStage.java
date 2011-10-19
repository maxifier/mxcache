package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.ClassVisitor;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 8:50:03
 */
public interface InstrumentationStage extends ClassVisitor {
    boolean isClassChanged();

    ClassVisitor getDetector();

    List<ClassDefinition> getAdditionalClasses();
}
