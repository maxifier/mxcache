package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 28.02.12
 * Time: 22:07
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
