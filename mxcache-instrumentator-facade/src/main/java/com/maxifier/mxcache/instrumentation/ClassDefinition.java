package com.maxifier.mxcache.instrumentation;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 23.03.2010
 * Time: 11:03:44
 */
public class ClassDefinition {
    private final String name;

    private final byte[] bytecode;

    public ClassDefinition(String name, byte[] bytecode) {
        this.name = name;
        this.bytecode = bytecode;
    }

    public String getName() {
        return name;
    }

    public byte[] getBytecode() {
        return bytecode;
    }
}