/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.util.ClassGenerator;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class ScalarTransformGenerator implements TransformGenerator {
    @Override
    public Signature transformKey(Signature in) {
        if (in.getContainer() == null) {
            throw new IllegalArgumentException("Cannot transform signature without key!");
        }
        int keyCount = in.getKeyCount();
        if (keyCount != 1) {
            throw new UnsupportedOperationException("Scalar transform cannot be applied to tuple");
        }
        return in.overrideKey(getOutType());
    }

    @Override
    public Signature transformValue(Signature in) {
        return in.overrideValue(getOutType());
    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {

    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {

    }
}
