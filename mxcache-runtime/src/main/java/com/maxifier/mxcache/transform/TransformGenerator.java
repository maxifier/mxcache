package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.util.ClassGenerator;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 13.09.2010
* Time: 16:29:07
*/
public interface TransformGenerator {
    TransformGenerator NO_TRANSFORM = EmptyTransformGenerator.getInstance();

    void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method);

    void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method);

    void generateFields(Type thisType, int fieldIndex, ClassGenerator writer);

    void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal);

    int getFieldCount();

    Class getTransformedType(Class in);
}
