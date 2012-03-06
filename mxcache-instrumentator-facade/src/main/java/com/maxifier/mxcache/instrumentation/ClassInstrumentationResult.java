package com.maxifier.mxcache.instrumentation;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 23.03.2010
 * Time: 11:00:11
 */
public class ClassInstrumentationResult {
    private final byte[] instrumentedBytecode;

    private final List<ClassDefinition> additionalClasses;

    public ClassInstrumentationResult(byte[] instrumentedBytecode, List<ClassDefinition> additionalClasses) {
        this.instrumentedBytecode = instrumentedBytecode;
        this.additionalClasses = additionalClasses;
    }

    public byte[] getInstrumentedBytecode() {
        return instrumentedBytecode;
    }

    public List<ClassDefinition> getAdditionalClasses() {
        return additionalClasses;
    }
}
