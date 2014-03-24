/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxConstructorGenerator extends MxGeneratorAdapter {
    private final Type superType;

    private boolean codeVisited;
    private boolean superCalled;
    private int startLocal;

    public MxConstructorGenerator(MethodVisitor mv, int access, Method method, Type thisClass, Type superType) {
        super(mv, access, method, thisClass);
        this.superType = superType;
    }

    @Override
    public void visitCode() {
        codeVisited = true;
        super.visitCode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (opcode == INVOKESPECIAL && owner.equals(superType.getInternalName()) && name.equals(CONSTRUCTOR_NAME)) {
            checkSuperCalled();
        }
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    private void checkSuperCalled() {
        if (superCalled) {
            throw new IllegalArgumentException("Super already called");
        }
        superCalled = true;
    }

    public void callSuper() {
        checkCodeVisited();
        loadThis();
        invokeConstructor(superType, NO_ARG_CONSTRUCTOR);
    }

    public void callSuper(Type... argumentTypes) {
        checkCodeVisited();
        loadThis();
        for (int i = argumentTypes.length; i != 0; i--) {
            loadArg(startLocal++);
        }
        invokeConstructor(superType, new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, argumentTypes));
    }

    private void checkCodeVisited() {
        if (!codeVisited) {
            visitCode();
        }
    }

    public void initFields(MxField... fields) {
        checkCodeVisited();
        for (MxField field : fields) {
            loadThis();
            loadArg(startLocal++);
            put(field);
        }
    }
}
