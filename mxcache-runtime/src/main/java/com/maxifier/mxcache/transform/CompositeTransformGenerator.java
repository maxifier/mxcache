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
public class CompositeTransformGenerator implements TransformGenerator {
    private final TransformGenerator forward;
    private final TransformGenerator backward;

    public CompositeTransformGenerator(@Nonnull TransformGenerator forward, @Nonnull TransformGenerator backward) {
        this.forward = forward;
        this.backward = backward;
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        forward.generateForward(thisType, fieldIndex, method);
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        backward.generateForward(thisType, forward.getFieldCount() + fieldIndex, method);
    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {
        forward.generateFields(thisType, fieldIndex, writer);
        backward.generateFields(thisType, forward.getFieldCount() + fieldIndex, writer);
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
        forward.generateAcquire(thisType, fieldIndex, ctor, contextLocal);
        backward.generateAcquire(thisType, forward.getFieldCount() + fieldIndex, ctor, contextLocal);
    }

    @Override
    public int getFieldCount() {
        return forward.getFieldCount() + backward.getFieldCount();
    }

    @Override
    public Class getTransformedType(Class in) {
        return forward.getTransformedType(in);
    }

    @Override
    public Signature transformKey(Signature in) {
        return forward.transformKey(in);
    }

    @Override
    public Signature transformValue(Signature in) {
        return forward.transformValue(in);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CompositeTransformGenerator that = (CompositeTransformGenerator) o;
        return forward.equals(that.forward) && backward.equals(that.backward);
    }

    @Override
    public int hashCode() {
        return 31 * forward.hashCode() + backward.hashCode();
    }

    @Override
    public String toString() {
        return "{forward = " + forward + ", backward = " + backward + "}";
    }
}
