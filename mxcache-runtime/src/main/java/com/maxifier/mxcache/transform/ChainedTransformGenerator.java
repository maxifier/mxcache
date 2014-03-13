/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.util.ClassGenerator;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public final class ChainedTransformGenerator implements TransformGenerator {
    private final TransformGenerator first;

    private final TransformGenerator second;

    private ChainedTransformGenerator(@Nonnull TransformGenerator first, @Nonnull TransformGenerator second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        first.generateForward(thisType, fieldIndex, method);
        second.generateForward(thisType, fieldIndex + first.getFieldCount(), method);
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        second.generateBackward(thisType, fieldIndex + first.getFieldCount(), method);
        first.generateBackward(thisType, fieldIndex, method);
    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {
        first.generateFields(thisType, fieldIndex, writer);
        second.generateFields(thisType, fieldIndex + first.getFieldCount(), writer);
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
        first.generateAcquire(thisType, fieldIndex, ctor, contextLocal);
        second.generateAcquire(thisType, fieldIndex + first.getFieldCount(), ctor, contextLocal);
    }

    @Override
    public int getFieldCount() {
        return first.getFieldCount() + second.getFieldCount();
    }

    @Override
    public Class getTransformedType(Class in) {
        return second.getTransformedType(first.getTransformedType(in));
    }

    @Override
    public Signature transformKey(Signature in) {
        return second.transformKey(first.transformKey(in));
    }

    @Override
    public Signature transformValue(Signature in) {
        return second.transformValue(first.transformValue(in));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChainedTransformGenerator that = (ChainedTransformGenerator) o;
        return first.equals(that.first) && second.equals(that.second);

    }

    @Override
    public int hashCode() {
        return 31 * first.hashCode() + second.hashCode();
    }

    @Override
    public String toString() {
        return first + " -> " + second;
    }

    @Nonnull
    public static TransformGenerator chain(@Nonnull TransformGenerator a, @Nonnull TransformGenerator b) {
        if (a == NO_TRANSFORM) {
            return b;
        }
        if (b == NO_TRANSFORM) {
            return a;
        }
        return new ChainedTransformGenerator(a, b);
    }
}
