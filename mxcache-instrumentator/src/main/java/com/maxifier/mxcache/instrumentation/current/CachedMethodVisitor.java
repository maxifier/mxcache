/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.ArgsWrapping;
import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.Context;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.AnnotationVisitor;
import com.maxifier.mxcache.asm.Type;

import static com.maxifier.mxcache.ArgsWrapping.*;
import static com.maxifier.mxcache.asm.Opcodes.*;

import java.lang.reflect.Modifier;

import com.maxifier.mxcache.instrumentation.IllegalCachedClass;
import com.maxifier.mxcache.util.Generator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import gnu.trove.strategy.HashingStrategy;
import gnu.trove.strategy.IdentityHashingStrategy;

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
    /** since 2.6.2: Hashing strategies are determined on instrumentation and passed into tuple constructor */
    private final boolean features262;
    /** since 2.6.2: For each argument a type of trove HashingStrategy or null for default strategy for argument type */
    private final Type[] hashingStrategies;
    private final Type[] argumentTypes;

    public CachedMethodVisitor(
            CachedInstrumentationStage classVisitor, MethodVisitor oldVisitor, int access, String name, String desc, String sign, String[] exceptions,
            Type thisClass, CachedMethodContext context, boolean features229, boolean features262
    ) {
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
        this.argumentTypes = Type.getArgumentTypes(desc);
        this.features262 = features262;
        this.hashingStrategies = new Type[argumentTypes.length];
    }

    @Override
    public AnnotationVisitor visitAnnotation(String annotationClassInnerName, boolean visible) {
        if (InstrumentatorImpl.CACHED_DESCRIPTOR.equals(annotationClassInnerName)) {
            return null;
        }
        return super.visitAnnotation(annotationClassInnerName, visible);
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, String annotationClassInnerName, boolean visible) {
        if (features262) {
            int paramTypeSort = argumentTypes[parameter].getSort();
            if (InstrumentatorImpl.HASHING_STRATEGY_DESCRIPTOR.equals(annotationClassInnerName)) {
                if (paramTypeSort != Type.OBJECT && paramTypeSort != Type.ARRAY) {
                    throw new IllegalCachedClass("Primitive types can't have custom hashing strategy", classVisitor.getSourceFileName());
                }
                return new AnnotationVisitor(Opcodes.ASM7, super.visitParameterAnnotation(parameter, annotationClassInnerName, visible)) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name)) {
                            hashingStrategies[parameter] = (Type) value;
                        }
                        super.visit(name, value);
                    }
                };
            }
            if (InstrumentatorImpl.IDENTITY_HASHING_DESCRIPTOR.equals(annotationClassInnerName)) {
                if (paramTypeSort != Type.OBJECT && paramTypeSort != Type.ARRAY) {
                    throw new IllegalCachedClass("Primitive types can't have custom hashing strategy", classVisitor.getSourceFileName());
                }
                hashingStrategies[parameter] = Type.getType(IdentityHashingStrategy.class);
            }
        }
        return super.visitParameterAnnotation(parameter, annotationClassInnerName, visible);
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
        Context context = new ContextImpl(id, isStatic, features262);
        StubMethodFactory.generate(thisClass, id, this, name, innerMethodName, desc, isStatic, hashingStrategies, context, features229, features262);
        endMethod();
        mv = classVisitor.visitTransparentMethod(isStatic ? ACC_STATIC : 0, innerMethodName, desc, sign, exceptions);
        mv.visitCode();
    }

    private class ContextImpl implements Context {
        private final int id;
        private final boolean isStatic;
        /** true since 2.6.2 */
        private final boolean staticHashingStrategies;

        public ContextImpl(int id, boolean isStatic, boolean staticHashingStrategies) {
            this.id = id;
            this.isStatic = isStatic;
            this.staticHashingStrategies = staticHashingStrategies;
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
            classVisitor.visitInnerClass(internalName, thisName, internalName.substring(thisName.length() + 1), ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC);
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
        public void registerCache(String cFieldName, String hsFieldName, Type cacheType, Type returnType, Type keyType, Type calculable, Generator cacheGetter) {
            addStaticInitializer(new RegisterCacheGenerator(keyType, returnType, calculable));

            classVisitor.addCacheGetter(id, isStatic, cacheGetter);
            classVisitor.visitTransparentField(
                    isStatic ? ACC_STATIC | CACHE_FIELD_ACCESSOR : CACHE_FIELD_ACCESSOR,
                    cFieldName, cacheType.getDescriptor(), null, null);

            if (ArgsWrapping.of(argumentTypes, hashingStrategies, staticHashingStrategies) == TUPLE_HS) {
                // static field for hashing strategies to be passed to Tuple constructor:
                classVisitor.visitTransparentField(
                        ACC_PRIVATE | ACC_STATIC | CACHE_FIELD_ACCESSOR,
                        hsFieldName, Type.getDescriptor(HashingStrategy[].class), null, null);
            }
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
