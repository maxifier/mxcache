/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.util.ClassGenerator;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public final class EmptyTransformGenerator implements TransformGenerator {
    private final Class<?> type;

    public EmptyTransformGenerator(Class<?> type) {
        this.type = type;
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public Class getOutType() {
        return type;
    }

    @Override
    public Class getInType() {
        return type;
    }

    @Override
    public Signature transformKey(Signature in) {
        return in;
    }

    @Override
    public Signature transformValue(Signature in) {
        return in;
    }

    @Override
    public String toString() {
        return "<no transform>";
    }
}
