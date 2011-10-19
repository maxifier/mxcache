package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.ClassGenerator;

import java.util.Arrays;

import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 16:19:27
*/
class TupleTransformGenerator implements TransformGenerator {
    private final Class[] inTypes;
    private final Class[] outTypes;
    private final TransformGenerator[] transformGenerators;
    
    private final int fieldCount;

    private final Class tupleIn;
    private final Type tupleInType;

    private final Class tupleOut;
    private final Type tupleOutType;

    public TupleTransformGenerator(TransformGenerator[] transformGenerators, Class[] inTypes, Class[] outTypes, Class tupleIn, Class tupleOut) {
        tupleInType = Type.getType(tupleIn);
        tupleOutType = Type.getType(tupleOut);

        this.transformGenerators = transformGenerators;
        this.outTypes = outTypes;
        this.inTypes = inTypes;
        this.tupleIn = tupleIn;
        this.tupleOut = tupleOut;

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
        method.newInstance(tupleOutType);
        method.dupX1();
        method.swap();
        for (int i = 0; i < transformGenerators.length; i++) {
            TransformGenerator transformGenerator = transformGenerators[i];
            Type from = Type.getType(inTypes[i]);
            Type to = Type.getType(outTypes[i]);
            boolean last = i == transformGenerators.length - 1;
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
        }
        method.invokeConstructor(tupleOutType, new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, toErasedTypes(outTypes)));
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        method.checkCast(tupleOutType);
        method.newInstance(tupleInType);
        method.dupX1();
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
        method.invokeConstructor(tupleInType, new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, toErasedTypes(inTypes)));
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
    public Class getTransformedType(Class in) {
        assert in == tupleIn: "Tuple type should match " + in + " and " + tupleIn;
        return tupleOut;
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
