package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

import static com.maxifier.mxcache.util.CodegenHelper.toClass;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 15.09.2010
* Time: 12:29:20
*/
public class UnboxTransformGenerator extends ScalarTransformGenerator {
    private final Type type;

    public UnboxTransformGenerator(Type type) {
        this.type = type;
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
    public Class getTransformedType(Class in) {
        return toClass(type);
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
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public String toString() {
        return "unbox " + type.getClassName();
    }
}
