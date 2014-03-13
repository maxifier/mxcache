/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
