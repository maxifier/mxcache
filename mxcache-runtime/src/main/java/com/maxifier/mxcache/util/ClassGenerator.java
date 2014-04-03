/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;

import static com.maxifier.mxcache.asm.Opcodes.ACC_PUBLIC;
import static com.maxifier.mxcache.asm.Opcodes.V1_5;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * ClassGenerator - a wrapper for ClassWriter with some useful methods.
 *
 * It simplifies class generation by providing higher-level functions for method definition, field definition, etc.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ClassGenerator extends ClassVisitor {
    private static final int DEFAULT_VERSION = V1_5;

    private Type superType;
    private Type thisType;
    private boolean endVisited;

    public ClassGenerator(int flags) {
        super(Opcodes.ASM4, new SmartClassWriter(flags));
    }

    public ClassGenerator(ClassReader classReader, int flags) {
        super(Opcodes.ASM4, new SmartClassWriter(classReader, flags));
    }

    public ClassGenerator(int access, String name, Class superType, Class... interfaces) {
        this(access, name, superType == null ? null : Type.getType(superType), (interfaces == null || interfaces.length == 0) ? null : CodegenHelper.getTypes(interfaces));
    }

    public ClassGenerator(int access, String name, Type superType, Type... interfaces) {
        this(DEFAULT_VERSION, access, name, superType, interfaces);
    }

    public ClassGenerator(int version, int access, String name, Type superType, Type... interfaces) {
        this(ClassWriter.COMPUTE_FRAMES);
        thisType = Type.getObjectType(name);
        this.superType = superType;

        String[] interfaceNames;
        if (interfaces != null && interfaces.length > 0) {
            interfaceNames = new String[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                interfaceNames[i] = interfaces[i].getInternalName();
            }
        } else {
            interfaceNames = null;
        }
        super.visit(version, access, name, null, superType == null ? null : superType.getInternalName(), interfaceNames);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        thisType = Type.getObjectType(name);
        superType = Type.getObjectType(superName);
        super.visit(version, access, name, signature, superName, interfaces);
    }

    public Type getThisType() {
        return thisType;
    }

    public ClassGenerator defineDefaultConstructor() {
        MxConstructorGenerator ctor = defineConstructor(ACC_PUBLIC);
        ctor.start();
        ctor.loadThis();
        ctor.invokeConstructor((superType == null ? OBJECT_TYPE : superType), NO_ARG_CONSTRUCTOR);
        ctor.returnValue();
        ctor.endMethod();
        return this;
    }

    public MxConstructorGenerator defineConstructor(int access, Type... arguments) {
        Method method = new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, arguments);
        MethodVisitor mv = visitMethod(access, method.getName(), method.getDescriptor(), null, null);
        return new MxConstructorGenerator(mv, access, method, thisType, superType);
    }

    public MxGeneratorAdapter defineMethod(int access, String name, Type returnType, Type... arguments) {
        return defineMethod(access, new Method(name, returnType, arguments));
    }

    public MxGeneratorAdapter defineMethod(int access, Method method) {
        MethodVisitor mv = visitMethod(access, method.getName(), method.getDescriptor(), null, null);
        return new MxGeneratorAdapter(mv, access, method, thisType);
    }

    public MxField defineField(int access, String name, Type type) {
        visitField(access, name, type.getDescriptor(), null, null);
        return new MxField(access, thisType, name, type);
    }

    public <T> Class<T> toClass(ClassLoader classLoader) {
        if (!endVisited) {
            visitEnd();
        }
        byte[] bytecode = toByteArray();
        return loadClass(classLoader, bytecode);
    }

    public byte[] toByteArray() {
        return ((SmartClassWriter)cv).toByteArray();
    }

    @Override
    public void visitEnd() {
        endVisited = true;
        super.visitEnd();
    }

    public void defineGetter(MxField field, String name) {
        MxGeneratorAdapter mv = defineMethod(ACC_PUBLIC, new Method(name, field.getType(), EMPTY_TYPES));
        mv.start();
        mv.get(field);
        mv.returnValue();
        mv.endMethod();
    }
}
