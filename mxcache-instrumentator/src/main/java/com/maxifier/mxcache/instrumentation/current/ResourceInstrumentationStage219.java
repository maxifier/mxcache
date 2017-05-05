/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassVisitor;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceInstrumentationStage219 extends ResourceInstrumentationStage {
    public ResourceInstrumentationStage219(InstrumentatorImpl instrumentator, ClassVisitor cv, ClassVisitor nextDetector) {
        super(instrumentator, cv, nextDetector);
    }

    @Override
    protected ResourceDetector createDetector(ClassVisitor nextDetector) {
        return new ResourceDetector(nextDetector, false);
    }
}
