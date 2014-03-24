/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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