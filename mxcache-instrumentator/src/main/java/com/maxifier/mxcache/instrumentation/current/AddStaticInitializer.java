/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.asm.ClassVisitor;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import com.maxifier.mxcache.asm.Opcodes;

import java.lang.reflect.Modifier;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.util.CodegenHelper.STATIC_INITIALIZER_ACCESS;
import static com.maxifier.mxcache.util.CodegenHelper.STATIC_INITIALIZER_NAME;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class AddStaticInitializer extends ClassVisitor {
    private final Method method;

    private boolean hasStaticInitializer;
    private Type thisType;

    public AddStaticInitializer(ClassVisitor cv, Method method) {
        super(Opcodes.ASM5, cv);
        this.method = method;
    }

    @Override
    public void visitEnd() {
        if (!hasStaticInitializer) {
            generateStaticInitializer();
        }
        super.visitEnd();
    }

    private void generateStaticInitializer() {
        MethodVisitor visitor = cv.visitMethod(STATIC_INITIALIZER_ACCESS, STATIC_INITIALIZER_NAME, "()V", null, null);
        visitor.visitCode();
        visitor.visitMethodInsn(INVOKESTATIC, thisType.getInternalName(), method.getName(), method.getDescriptor());
        visitor.visitInsn(RETURN);
        visitor.visitMaxs(0, 0);
        visitor.visitEnd();
    }

    @Override
    public void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
        thisType = Type.getObjectType(className);
        super.visit(version, access, className, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, String sign, String[] exceptions) {
        MethodVisitor superVisitor = super.visitMethod(access, name, desc, sign, exceptions);
        if (Modifier.isStatic(access) && name.equals(STATIC_INITIALIZER_NAME)) {
            hasStaticInitializer = true;
            return new MxGeneratorAdapter(superVisitor, access, name, desc, thisType) {
                @Override
                public void visitCode() {
                    super.visitCode();
                    if (thisType == null) {
                        throw new IllegalArgumentException("No type");
                    }
                    invokeStatic(thisType, method);
                }
            };
        }
        return superVisitor;
    }
}
