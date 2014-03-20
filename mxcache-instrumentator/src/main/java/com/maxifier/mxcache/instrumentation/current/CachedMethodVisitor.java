/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.Context;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.asm.Type;
import static com.maxifier.mxcache.asm.Opcodes.*;

import java.lang.reflect.Modifier;

import com.maxifier.mxcache.util.Generator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class CachedMethodVisitor extends MxGeneratorAdapter {

    private static final int CACHE_FIELD_ACCESSOR = ACC_SYNTHETIC | ACC_TRANSIENT;

    private final CachedMethodContext context;

    private final Type thisClass;
    private final int access;
    private final String name;
    private final String desc;
    private final String sign;
    private final String[] exceptions;
    private final CachedInstrumentationStage classVisitor;
    private final boolean features229;

    public CachedMethodVisitor(CachedInstrumentationStage classVisitor, MethodVisitor oldVisitor, int access, String name, String desc, String sign, String[] exceptions, Type thisClass, CachedMethodContext context, boolean features229) {
        super(oldVisitor, access, name, desc, thisClass);
        this.classVisitor = classVisitor;
        this.access = access;
        this.name = name;
        this.desc = desc;
        this.sign = sign;
        this.exceptions = exceptions;
        this.thisClass = thisClass;
        this.context = context;
        this.features229 = features229;
    }

    @Override
    public AnnotationVisitor visitAnnotation(String annotationClassInnerName, boolean visible) {
        if (InstrumentatorImpl.CACHED_DESCRIPTOR.equals(annotationClassInnerName)) {
            return null;
        }
        return super.visitAnnotation(annotationClassInnerName, visible);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        int id = classVisitor.nextCache();

        boolean isStatic = Modifier.isStatic(access);

        for (String tag : context.getTags()) {
            classVisitor.appendTagList(id, isStatic, tag);
        }
        String group = context.getGroup();
        if (group != null) {
            classVisitor.appendGroupList(id, isStatic, group);
        }

        String innerMethodName = name + "$create";
        Context context = new ContextImpl(id, isStatic);
        StubMethodFactory.generate(thisClass, id, this, name, innerMethodName, desc, isStatic, context, features229);
        endMethod();
        mv = classVisitor.visitTransparentMethod(isStatic ? ACC_STATIC : 0, innerMethodName, desc, sign, exceptions);
        mv.visitCode();
    }

    private class ContextImpl implements Context {
        private final int id;
        private final boolean isStatic;

        public ContextImpl(int id, boolean isStatic) {
            this.id = id;
            this.isStatic = isStatic;
        }

        @Override
        public void define(Type type, byte[] byteCode) {
            classVisitor.getAdditionalClasses().add(new ClassDefinition(type.getClassName(), byteCode));
        }

        @Override
        public void innerClass(Type innerType) {
            String internalName = innerType.getInternalName();
            String thisName = thisClass.getInternalName();
            assert internalName.startsWith(thisName + "$");
            assert Character.isJavaIdentifierStart(internalName.charAt(thisName.length() + 1));
            classVisitor.visitInnerClass(internalName, thisName, internalName.substring(thisName.length() + 1), ACC_PRIVATE);
        }

        @Override
        public void addInstanceInitializer(Generator g) {
            classVisitor.addInstanceInitializer(g);
        }

        @Override
        public void addStaticInitializer(Generator g) {
            classVisitor.addStaticInitializer(g);
        }

        @Override
        public void registerCache(String fieldName, Type cacheType, final Type returnType, final Type keyType, final Type calculable, Generator cacheGetter) {
            addStaticInitializer(new RegisterCacheGenerator(keyType, returnType, calculable));

            classVisitor.addCacheGetter(id, isStatic, cacheGetter);

            classVisitor.visitTransparentField(isStatic ? ACC_STATIC | CACHE_FIELD_ACCESSOR : CACHE_FIELD_ACCESSOR, fieldName, cacheType.getDescriptor(), null, null);
        }

        private class RegisterCacheGenerator extends Generator {
            private final Type keyType;
            private final Type returnType;
            private final Type calculatableType;

            public RegisterCacheGenerator(Type keyType, Type returnType, Type calculatableType) {
                this.keyType = keyType;
                this.returnType = returnType;
                this.calculatableType = calculatableType;
            }

            @Override
            public void generate(MxGeneratorAdapter mv) {
                mv.push(thisClass);
                mv.push(id);
                mv.push(keyType);
                mv.push(returnType);
                mv.push(context.getGroup());

                if (context.getTags().isEmpty()) {
                    mv.pushNull();
                } else {
                    mv.push(context.getTags());
                }

                mv.newInstance(calculatableType);
                mv.dup();
                mv.invokeConstructor(calculatableType, NO_ARG_CONSTRUCTOR);
                mv.push(name);
                mv.push(desc);
                if (features229) {
                    mv.push(context.getName());
                    mv.invokeStatic(RuntimeTypes.CACHE_FACTORY_TYPE, RuntimeTypes.FACTORY_REGISTER_CACHE_METHOD);
                } else {
                    mv.invokeStatic(RuntimeTypes.CACHE_FACTORY_TYPE, RuntimeTypes.FACTORY_REGISTER_CACHE_METHOD_OLD);
                }
            }
        }
    }
}
