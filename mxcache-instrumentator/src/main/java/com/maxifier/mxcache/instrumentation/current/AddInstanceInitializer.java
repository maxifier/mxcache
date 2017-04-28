/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.AdviceAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.CodegenHelper;

import java.lang.reflect.Modifier;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class AddInstanceInitializer extends ClassVisitor {
    private final Method method;

    private Type thisType;

    public AddInstanceInitializer(ClassVisitor cv, Method method) {
        super(Opcodes.ASM5, cv);
        this.method = method;
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        thisType = Type.getObjectType(className);
        super.visit(version, access, className, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, String sign, String[] exceptions) {
        if (!Modifier.isStatic(access) && name.equals(CodegenHelper.CONSTRUCTOR_NAME)) {
            return new AdviceAdapter(Opcodes.ASM5, super.visitMethod(access, name, desc, sign, exceptions), access, name, desc) {
                @Override
                protected void onMethodEnter() {
                    loadThis();
                    invokeVirtual(thisType, method);
                }
            };
        }
        return super.visitMethod(access, name, desc, sign, exceptions);
    }
}
