/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class IgnoreTransformGenerator extends ScalarTransformGenerator {
    private final Class<?> type;

    public IgnoreTransformGenerator(Class<?> type) {
        this.type = type;
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        if (thisType.getSize() == 2) {
            method.pop2();
        } else {
            method.pop();
        }
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class getOutType() {
        return null;
    }

    @Override
    public Class<?> getInType() {
        return type;
    }
}
