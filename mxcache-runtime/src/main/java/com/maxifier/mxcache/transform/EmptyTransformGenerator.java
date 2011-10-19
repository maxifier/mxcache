package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 16:12:44
*/
final class EmptyTransformGenerator implements TransformGenerator {
    private static final TransformGenerator INSTANCE = new EmptyTransformGenerator();

    public static TransformGenerator getInstance() {
        return INSTANCE;
    }

    private EmptyTransformGenerator() {
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
    public Class getTransformedType(Class in) {
        return in;
    }

    @Override
    public String toString() {
        return "<no transform>";
    }
}
