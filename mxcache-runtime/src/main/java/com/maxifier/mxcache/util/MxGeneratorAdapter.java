/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import gnu.trove.list.array.TIntArrayList;

import java.lang.reflect.Modifier;
import java.util.Collection;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.commons.Method.getMethod;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxGeneratorAdapter extends GeneratorAdapter {
    public static final Method TO_STRING_METHOD = getMethod("java.lang.String toString()");

    private final Type thisClass;
    private final boolean isStatic;

    public void push(TIntArrayList list) {
        push(list.size());
        newArray(Type.INT_TYPE);
        for (int i = 0; i<list.size(); i++) {
            dup();
            push(i);
            push(list.get(i));
            arrayStore(Type.INT_TYPE);
        }
    }

    public void push(Collection<String> c) {
        push(c.size());
        newArray(CodegenHelper.STRING_TYPE);
        int i = 0;
        for (String s : c) {
            dup();
            push(i++);
            push(s);
            arrayStore(CodegenHelper.STRING_TYPE);
        }
    }

    public MxGeneratorAdapter(int access, Method method, Type thisClass, ClassVisitor cv) {
        super(Opcodes.ASM7, cv.visitMethod(access, method.getName(), method.getDescriptor(), null, null), access, method.getName(), method.getDescriptor());
        this.thisClass = thisClass;
        isStatic = Modifier.isStatic(access);
    }

    public MxGeneratorAdapter(int access, Method method, MethodVisitor mv, Type thisClass) {
        super(Opcodes.ASM7, mv, access, method.getName(), method.getDescriptor());
        this.thisClass = thisClass;
        isStatic = Modifier.isStatic(access);
    }

    public MxGeneratorAdapter(MethodVisitor mv, int access, Method method, Type thisClass) {
        super(Opcodes.ASM7, mv, access, method.getName(), method.getDescriptor());
        this.thisClass = thisClass;
        isStatic = Modifier.isStatic(access);
    }

    public MxGeneratorAdapter(MethodVisitor mv, int access, String name, String desc, Type thisClass) {
        super(Opcodes.ASM7, mv, access, name, desc);
        this.thisClass = thisClass;
        isStatic = Modifier.isStatic(access);
    }

    public void get(MxField field) {
        if (!field.getOwner().equals(thisClass)) {
            throw new IllegalArgumentException("Cannot get field of another class");
        }
        if (isStatic && !field.isStatic()) {
            throw new IllegalArgumentException("Cannot access non-static field from static context");
        }
        if (field.isStatic()) {
            getStatic(thisClass, field.getName(), field.getType());
        } else {
            loadThis();
            getField(thisClass, field.getName(), field.getType());
        }
    }

    public void get(String field, Type type) {
        if (isStatic) {
            getStatic(thisClass, field, type);
        } else {
            loadThis();
            getField(thisClass, field, type);
        }
    }

    public void put(MxField field) {
        if (!field.getOwner().equals(thisClass)) {
            throw new IllegalArgumentException("Cannot get field of another class");
        }
        if (isStatic && !Modifier.isStatic(field.getAccess())) {
            throw new IllegalArgumentException("Cannot access non-static field from static context");
        }
        put(field.getName(), field.getType());
    }

    public void put(String field, Type type) {
        if (isStatic) {
            putStatic(thisClass, field, type);
        } else {
            putField(thisClass, field, type);
        }
    }

    public void pushNull() {
        visitInsn(Opcodes.ACONST_NULL);
    }

    public void dup(Type t) {
        visitInsn(t.getSize() == 2 ? DUP2 : DUP);
    }

    public void pop(Type t) {
        visitInsn(t.getSize() == 2 ? POP2 : POP);
    }

    public void start() {
        visitCode();
    }

    public void createWithDefaultConstructor(Type t) {
        newInstance(t);
        dup();
        invokeConstructor(t, CodegenHelper.NO_ARG_CONSTRUCTOR);
    }

    public void invokeToString() {
        invokeVirtual(CodegenHelper.OBJECT_TYPE, TO_STRING_METHOD);
    }
}
