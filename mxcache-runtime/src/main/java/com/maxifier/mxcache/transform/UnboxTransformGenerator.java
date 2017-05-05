/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
public class UnboxTransformGenerator extends ScalarTransformGenerator {
    private final Class cls;
    private final Type type;

    public UnboxTransformGenerator(Class cls) {
        if (!cls.isPrimitive()) {
            throw new IllegalArgumentException("Only primitives can be boxed");
        }
        this.cls = cls;
        this.type = Type.getType(cls);
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        method.unbox(type);
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        method.box(type);
    }

    @Override
    public Class getOutType() {
        return cls;
    }

    @Override
    public Class<?> getInType() {
        return Object.class;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UnboxTransformGenerator that = (UnboxTransformGenerator) o;
        return cls.equals(that.cls);

    }

    @Override
    public int hashCode() {
        return cls.hashCode();
    }

    @Override
    public String toString() {
        return "unbox " + cls.getSimpleName();
    }
}
