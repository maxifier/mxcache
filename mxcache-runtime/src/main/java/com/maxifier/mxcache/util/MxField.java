/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxField {
    private final int access;
    private final Type owner;
    private final String name;
    private final Type type;

    public MxField(int access, Type owner, String name, Type type) {
        this.access = access;
        this.owner = owner;
        this.name = name;
        this.type = type;
    }

    public int getAccess() {
        return access;
    }

    public Type getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public void define(ClassVisitor v) {
        v.visitField(access, name, type.getDescriptor(), null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MxField mxField = (MxField) o;

        return access == mxField.access &&
                name.equals(mxField.name) &&
                owner.equals(mxField.owner) &&
                type.equals(mxField.type);

    }

    @Override
    public int hashCode() {
        int result = access;
        result = 31 * result + owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    public boolean isStatic() {
        return (access & Opcodes.ACC_STATIC) != 0;
    }
}
