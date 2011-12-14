package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 12/14/11
 * Time: 11:49 AM
 */
public class IgnoreTransformGenerator extends ScalarTransformGenerator {
    private static final TransformGenerator INSTANCE = new IgnoreTransformGenerator();

    public static TransformGenerator getInstance() {
        return INSTANCE;
    }

    private IgnoreTransformGenerator() {
    }

    @Override
    public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        if (thisType.getSize() == 2) {
            method.pop2();
        } else {
            method.pop();
        }
    }

    @Override
    public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
        throw new UnsupportedOperationException();
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
        return null;
    }
}
