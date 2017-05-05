/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.tuple.TupleGenerator;
import com.maxifier.mxcache.util.ClassGenerator;
import gnu.trove.strategy.HashingStrategy;

import java.util.Arrays;

import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class TupleTransformGenerator implements TransformGenerator {
    private final Class[] inTypes;
    private final Class[] outTypes;
    private final TransformGenerator[] transformGenerators;
    
    private final int fieldCount;

    private final Class tupleIn;
    private final Type tupleInType;

    private final Class container;
    private final Class tupleOut;
    private final Type tupleOutType;

    public TupleTransformGenerator(TransformGenerator[] transformGenerators, Class[] inTypes, Class[] outTypes) {
        this.transformGenerators = transformGenerators;
        this.outTypes = outTypes;
        this.inTypes = inTypes;

        tupleIn = TupleGenerator.getTupleClass(inTypes);
        tupleInType = Type.getType(tupleIn);

        if (outTypes.length > 1) {
            tupleOut = TupleGenerator.getTupleClass(outTypes);
            tupleOutType = Type.getType(tupleOut);
            container = tupleOut;
        } else {
            tupleOut = null;
            tupleOutType = null;
            container = outTypes.length == 0 ? null : outTypes[0];
        }

        this.fieldCount = getFieldCount(transformGenerators);
    }

    private static int getFieldCount(TransformGenerator[] transformGenerators) {
        int fieldCount = 0;
        for (TransformGenerator transformGenerator : transformGenerators) {
            if (transformGenerator != null) {
                fieldCount += transformGenerator.getFieldCount();
            }
        }
        return fieldCount;
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        method.checkCast(tupleInType);
        if (tupleOut != null) {
            method.newInstance(tupleOutType);
            method.dupX1();
            method.swap();
            method.dup();
            method.invokeVirtual(tupleInType, TupleGenerator.TUPLE_GET_HASHING_STRATEGIES);
            method.swap();
        }
        for (int i = 0, j = 0; i < transformGenerators.length; i++) {
            TransformGenerator transformGenerator = transformGenerators[i];
            if (!(transformGenerator instanceof IgnoreTransformGenerator)) {
                Type from = Type.getType(inTypes[i]);
                Type to = Type.getType(outTypes[j]);
                boolean last = j == outTypes.length - 1;
                if (!last) {
                    method.dup();
                }
                method.invokeVirtual(tupleInType, new Method("getElement" + i, erase(from), EMPTY_TYPES));
                if (isReferenceType(from)) {
                    method.checkCast(from);
                }
                if (transformGenerator != null) {
                    transformGenerator.generateForward(thisType, fieldIndex, method);
                    fieldIndex += transformGenerator.getFieldCount();
                }
                if (!last) {
                    method.swap(OBJECT_TYPE, to);
                }
                j++;
            }
        }
        if (tupleOut != null) {
            Type[] ctorArgs = new Type[outTypes.length + 1];
            System.arraycopy(toErasedTypes(outTypes), 0, ctorArgs, 1, outTypes.length);
            ctorArgs[0] = Type.getType(HashingStrategy[].class);
            method.invokeConstructor(tupleOutType, new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, ctorArgs));
        }
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        if (inTypes.length != outTypes.length) {
            throw new UnsupportedOperationException("There are ignored params");
        }
        method.checkCast(tupleOutType);
        method.newInstance(tupleInType);
        method.dupX1();
        method.swap();
        method.dup();
        method.invokeVirtual(tupleOutType, TupleGenerator.TUPLE_GET_HASHING_STRATEGIES);
        method.swap();
        for (int i = 0; i < transformGenerators.length; i++) {
            TransformGenerator transformGenerator = transformGenerators[i];
            Type from = Type.getType(outTypes[i]);
            Type to = Type.getType(inTypes[i]);
            boolean last = i == transformGenerators.length - 1;
            if (!last) {
                method.dup();
            }
            method.invokeVirtual(tupleOutType, new Method("getElement" + i, erase(from), EMPTY_TYPES));
            if (isReferenceType(from)) {
                method.checkCast(from);
            }
            if (transformGenerator != null) {
                transformGenerator.generateBackward(thisType, fieldIndex, method);
                fieldIndex += transformGenerator.getFieldCount();
            }
            if (!last) {
                method.swap(OBJECT_TYPE, to);
            }
        }
        Type[] ctorArgs = new Type[inTypes.length + 1];
        System.arraycopy(toErasedTypes(inTypes), 0, ctorArgs, 1, inTypes.length);
        ctorArgs[0] = Type.getType(HashingStrategy[].class);
        method.invokeConstructor(tupleInType, new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, ctorArgs));
    }

    @Override
    public void generateFields(Type thisType, int fieldIndex, final ClassGenerator writer) {
        for (TransformGenerator transformGenerator : transformGenerators) {
            if (transformGenerator != null) {
                transformGenerator.generateFields(thisType, fieldIndex, writer);
                fieldIndex += transformGenerator.getFieldCount();
            }
        }
    }

    @Override
    public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
        for (TransformGenerator transformGenerator : transformGenerators) {
            if (transformGenerator != null) {
                transformGenerator.generateAcquire(thisType, fieldIndex, ctor, contextLocal);
                fieldIndex += transformGenerator.getFieldCount();
            }
        }
    }

    @Override
    public int getFieldCount() {
        return fieldCount;
    }

    @Override
    public Class getInType() {
        return tupleIn;
    }

    @Override
    public Class getOutType() {
        return container;
    }

    @Override
    public Signature transformKey(Signature in) {
        assert in.getContainer() == tupleIn: "Tuple type should match " + in + " and " + tupleIn;
        return new Signature(outTypes, container, in.getValue());
    }

    @Override
    public Signature transformValue(Signature in) {
        throw new UnsupportedOperationException("Value cannot be tuple transform");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TupleTransformGenerator that = (TupleTransformGenerator) o;
        return Arrays.equals(inTypes, that.inTypes) && Arrays.equals(transformGenerators, that.transformGenerators);

    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(inTypes) + Arrays.hashCode(transformGenerators);
    }

    @Override
    public String toString() {
        return "tuple transform " + Arrays.toString(transformGenerators);
    }
}
