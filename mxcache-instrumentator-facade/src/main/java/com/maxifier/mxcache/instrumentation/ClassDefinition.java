package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.asm.Type;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 23.03.2010
 * Time: 11:03:44
 */
public class ClassDefinition {
    private final Type type;

    private final byte[] bytecode;

    public ClassDefinition(Type type, byte[] bytecode) {
        this.type = type;
        this.bytecode = bytecode;
    }

    public Type getType() {
        return type;
    }

    public byte[] getBytecode() {
        return bytecode;
    }
}