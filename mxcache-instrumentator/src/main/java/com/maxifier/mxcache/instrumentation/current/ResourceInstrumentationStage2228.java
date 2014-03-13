/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassVisitor;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceInstrumentationStage2228 extends ResourceInstrumentationStage {
    public ResourceInstrumentationStage2228(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(instrumentator, cv, nextDetector);
    }

    @Override
    protected ResourceDetector createDetector(ClassVisitor nextDetector) {
        return new ResourceDetector(nextDetector, true);
    }
}
