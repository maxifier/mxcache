/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.util.ClassGenerator;

import javax.annotation.Nullable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public interface TransformGenerator {
    void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method);

    void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method);

    void generateFields(Type thisType, int fieldIndex, ClassGenerator writer);

    void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal);

    int getFieldCount();

    Signature transformKey(Signature in);

    Signature transformValue(Signature in);

    Class<?> getInType();

    @Nullable
    Class<?> getOutType();
}
