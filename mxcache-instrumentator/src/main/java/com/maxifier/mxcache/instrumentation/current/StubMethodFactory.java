/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import static com.maxifier.mxcache.ArgsWrapping.TUPLE_HS;
import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.*;

import com.maxifier.mxcache.asm.Label;
import com.maxifier.mxcache.impl.CalculatableHelper;
import com.maxifier.mxcache.ArgsWrapping;
import com.maxifier.mxcache.instrumentation.Context;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.tuple.TupleGenerator;

import static com.maxifier.mxcache.ArgsWrapping.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.Generator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import gnu.trove.strategy.HashingStrategy;

import javax.annotation.Nullable;

/**
 * StubMethodFactory
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class StubMethodFactory {

    private static final String CACHE_FIELD_POSTFIX = "$cache$";
    private static final String HASHING_STRAT_POSTFIX = "$hashingStrategies$";

    private static final String[] TYPE_NAME_BY_SORT = createNamesArray();

    private static String[] createNamesArray() {
        // OBJECT is max id
        String[] res = new String[OBJECT + 1];
        res[BOOLEAN] = "Boolean";
        res[CHAR] = "Char";
        res[BYTE] = "Byte";
        res[SHORT] = "Short";
        res[INT] = "Int";
        res[Type.FLOAT] = "Float";
        res[Type.LONG] = "Long";
        res[Type.DOUBLE] = "Double";
        res[ARRAY] = "Object";
        res[OBJECT] = "Object";
        return res;
    }

    private StubMethodFactory() {
    }

    public static void generate(
            Type thisClass, int id, MxGeneratorAdapter methodVisitor, String methodName, String innerMethodName, String methodDescriptor,
            boolean isStatic, Type[] argsHashingStrats, Context context, boolean customContext, boolean staticHashingStrategies
    ) {
        Type returnType = getReturnType(methodDescriptor);
        // assertion cause it should be detected earlier
        assert returnType != VOID_TYPE: "Void method cannot be cached!";
        String cacheFieldName = methodName + CACHE_FIELD_POSTFIX + id;
        String hashingStratsFieldName = methodName + HASHING_STRAT_POSTFIX + id;

        Type[] args = getArgumentTypes(methodDescriptor);
        ArgsWrapping argsWrapping = ArgsWrapping.of(args, argsHashingStrats, staticHashingStrategies);

        if (argsWrapping == TUPLE) {
            // our class should initialize tuple classes that it uses as they don't exist already
            context.addStaticInitializer(new TupleInitializerGenerator(args));
        } else if (argsWrapping == TUPLE_HS) {
            // our class should initialize tuple classes that it uses as they don't exist already
            context.addStaticInitializer(new TupleInitializerGenerator(args));
            // initialize static hashingStrategies field:
            context.addStaticInitializer(new InitHashingStratGenerator(args, argsHashingStrats, thisClass, hashingStratsFieldName));
        }

        Type keyType = getKeyType(args, argsWrapping);
        Type cacheType = getCacheType(keyType, returnType);

        Type calculatableType = generateCalculable(
                id, methodName, innerMethodName, methodDescriptor, argsWrapping, returnType, context, thisClass, keyType, isStatic);

        context.registerCache(cacheFieldName, hashingStratsFieldName, cacheType, returnType, keyType, calculatableType,
                              new GetCacheGenerator(isStatic, thisClass, cacheFieldName, cacheType));

        InitializerGenerator initializer = new InitializerGenerator(id, thisClass, cacheFieldName, cacheType, isStatic, customContext);
        if (isStatic) {
            context.addStaticInitializer(initializer);
        } else {
            context.addInstanceInitializer(initializer);
        }

        methodVisitor.get(cacheFieldName, cacheType);

        methodVisitor.dup();
        Label cacheInitialized = new Label();
        methodVisitor.ifNonNull(cacheInitialized);

        methodVisitor.throwException(Type.getType(IllegalStateException.class), "@Cached method " + thisClass.getClassName() + "#" + methodName + methodDescriptor + " is called before cache is initialized.\n" +
                "It usually happens when @Cached method overrides superclass method, and this method is called from superclass constructor somehow before\n" +
                "constructor of class containing the cache finished, e.g. when superclass constructor invokes SwingWorker that makes use of this cached method");

        methodVisitor.mark(cacheInitialized);

        Type[] getOrCreateArgTypes = generatePrepareGetOrCreateArgs(thisClass, methodVisitor, hashingStratsFieldName, argsWrapping, args, keyType);
        methodVisitor.invokeInterface(cacheType, new Method("getOrCreate", eraseType(returnType), getOrCreateArgTypes));
        if (isReferenceType(returnType)) {
            methodVisitor.checkCast(returnType);
        }
        methodVisitor.returnValue();
    }

    private static Type[] generatePrepareGetOrCreateArgs(
            Type thisClass, MxGeneratorAdapter methodVisitor,
            String hashingStratsFieldName, ArgsWrapping argsWrapping, Type[] args, Type keyType
    ) {
        switch (argsWrapping) {
            case EMPTY:
                return EMPTY_TYPES;
            case RAW:
                methodVisitor.loadArg(0);
                return new Type[] { eraseType(keyType) };
            case TUPLE:
                generateWrapOld(methodVisitor, args, keyType);
                return new Type[] { eraseType(keyType) };
            case TUPLE_HS:
                generateWrap(thisClass, methodVisitor, hashingStratsFieldName, args, keyType);
                return new Type[] { eraseType(keyType) };
            default:
                throw new AssertionError();
        }
    }

    @Nullable
    private static Type getKeyType(Type[] args, ArgsWrapping argsWrapping) {
        switch (argsWrapping) {
            case EMPTY:
                return null;
            case RAW:
                return args[0];
            case TUPLE:
            case TUPLE_HS:
                return getObjectType(TupleGenerator.getTupleClassName(args));
            default:
                throw new AssertionError();
        }
    }

    private static Type generateCalculable(
            int id, String methodName, String innerMethodName, String methodDescriptor, ArgsWrapping argsWrapping, Type returnType,
            Context context, Type thisClass, Type keyType, boolean isStatic
    ) {
        String calculable = CalculatableHelper.getCalculatableName(thisClass, methodName, id);

        Type superType = getCalculatableType(keyType, returnType);
        ClassGenerator w = new ClassGenerator(ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC | ACC_FINAL, calculable, OBJECT_TYPE, superType);

        Type calculatableType = w.getThisType();

        w.visitOuterClass(thisClass.getInternalName(), null, null);
        context.innerClass(calculatableType);

        w.defineDefaultConstructor();

        MxGeneratorAdapter mv = w.defineMethod(ACC_PUBLIC, "calculate", erase(returnType), getCalculateMethodDescriptor(keyType));
        mv.start();
        if (!isStatic) {
            mv.loadArg(0);
            mv.checkCast(thisClass);
        }
        if (keyType != null) {
            Type[] types = Type.getArgumentTypes(methodDescriptor);
            mv.loadArg(1);
            if (argsWrapping == TUPLE || argsWrapping == TUPLE_HS) {
                generateUnwrap(keyType, mv, types);
            } else {
                assert types.length == 1;
                Type onlyArgumentType = types[0];
                if (isReferenceType(onlyArgumentType)) {
                    mv.checkCast(onlyArgumentType);
                }
            }
        }
        mv.visitMethodInsn(isStatic ? INVOKESTATIC : INVOKEVIRTUAL, thisClass.getInternalName(), innerMethodName, methodDescriptor);
        mv.returnValue();
        mv.endMethod();

        w.visitEnd();

        context.define(calculatableType, w.toByteArray());
        return calculatableType;
    }

    private static Type[] getCalculateMethodDescriptor(Type keyType) {
        if (keyType == null) {
            return new Type[] { OBJECT_TYPE };
        }
        return new Type[] { OBJECT_TYPE, eraseType(keyType) };
    }

    private static Type getCacheType(Type keyType, Type valueType) {
        return getCacheType(keyType, valueType, "Cache");
    }

    private static Type getCalculatableType(Type keyType, Type valueType) {
        return getCacheType(keyType, valueType, "Calculatable");
    }

    private static Type getCacheType(Type keyType, Type valueType, String postfix) {
        if (keyType == null) {
            return getObjectType("com/maxifier/mxcache/caches/" + getName(valueType) + postfix);
        }
        return getObjectType("com/maxifier/mxcache/caches/" + getName(keyType) + getName(valueType) + postfix);
    }

    private static String getName(Type type) {
        int sort = type.getSort();
        if (sort >= 0 && sort < TYPE_NAME_BY_SORT.length) {
            String name = TYPE_NAME_BY_SORT[sort];
            if (name != null) {
                return name;
            }
        }
        throw new UnsupportedOperationException("Unknown type " + type);
    }

    private static void generateUnwrap(Type keyType, MxGeneratorAdapter mv, Type[] types) {
        mv.checkCast(keyType);

        int local = mv.newLocal(keyType);
        mv.storeLocal(local);
        for (int i = 0; i < types.length; i++) {
            mv.loadLocal(local);
            Type type = types[i];
            mv.invokeVirtual(keyType, new Method("getElement" + i, eraseType(type), new Type[] { }));
            if (isReferenceType(type)) {
                mv.checkCast(type);
            }
        }
    }

    private static void generateWrap(Type thisClass, MxGeneratorAdapter mv, String hashingStratsFieldName, Type[] args, Type tupleType) {
        mv.newInstance(tupleType);
        mv.dup();
        Type hashingStrategiesType = Type.getType(HashingStrategy[].class);
        mv.getStatic(thisClass, hashingStratsFieldName, hashingStrategiesType);
        Type[] tupleConstructorTypes = new Type[args.length + 1];
        tupleConstructorTypes[0] = hashingStrategiesType;
        for (int i = 1; i < tupleConstructorTypes.length; i++) {
            tupleConstructorTypes[i] = eraseType(args[i-1]);
            mv.loadArg(i-1);
        }
        mv.invokeConstructor(tupleType, new Method(CONSTRUCTOR_NAME, VOID_TYPE, tupleConstructorTypes));
    }

    /** Before 2.6.2 only */
    private static void generateWrapOld(MxGeneratorAdapter mv, Type[] args, Type tupleType) {
        mv.newInstance(tupleType);
        mv.dup();
        Type[] tupleConstructorTypes = new Type[args.length];
        for (int i = 0; i < args.length; i++) {
            tupleConstructorTypes[i] = eraseType(args[i]);
            mv.loadArg(i);
        }
        mv.invokeConstructor(tupleType, new Method(CONSTRUCTOR_NAME, VOID_TYPE, tupleConstructorTypes));
    }

    private static Type eraseType(Type type) {
        return isReferenceType(type) ? OBJECT_TYPE : type;
    }

    private static boolean isReferenceType(Type type) {
        int sort = type.getSort();
        return sort == OBJECT || sort == ARRAY;
    }

    private static class InitializerGenerator extends Generator {
        private final int id;
        private final Type thisClass;
        private final String cacheFieldName;
        private final Type cacheType;
        private final boolean isStatic;

        private final boolean customContext;

        public InitializerGenerator(int id, Type thisClass, String cacheFieldName, Type cacheType, boolean isStatic, boolean customContext) {
            this.id = id;
            this.thisClass = thisClass;
            this.cacheFieldName = cacheFieldName;
            this.cacheType = cacheType;
            this.isStatic = isStatic;
            this.customContext = customContext;
        }

        @Override
        public void generate(MxGeneratorAdapter mv) {
            Label skipInitialization;
            if (isStatic) {
                skipInitialization = null;
            } else {
                skipInitialization = new Label();
                mv.loadThis();
                mv.get(cacheFieldName, cacheType);
                mv.ifNonNull(skipInitialization);
                mv.loadThis();
            }
            mv.push(thisClass);
            mv.push(id);
            if (isStatic) {
                mv.pushNull();
            } else {
                mv.loadThis();
            }
            if (customContext) {
                mv.loadArg(0);
                mv.invokeStatic(RuntimeTypes.CACHE_FACTORY_TYPE, RuntimeTypes.CREATE_CACHE_METHOD);
            } else {
                mv.invokeStatic(RuntimeTypes.CACHE_FACTORY_TYPE, RuntimeTypes.CREATE_CACHE_METHOD_OLD);
            }
            mv.checkCast(cacheType);
            mv.put(cacheFieldName, cacheType);
            if (!isStatic) {
                mv.mark(skipInitialization);
            }
        }
    }

    private static class GetCacheGenerator extends Generator {
        private final boolean isStatic;
        private final Type thisClass;
        private final String cacheFieldName;
        private final Type cacheType;

        public GetCacheGenerator(boolean isStatic, Type thisClass, String cacheFieldName, Type cacheType) {
            this.isStatic = isStatic;
            this.thisClass = thisClass;
            this.cacheFieldName = cacheFieldName;
            this.cacheType = cacheType;
        }

        @Override
        public void generate(MxGeneratorAdapter m) {
            if (isStatic) {
                m.getStatic(thisClass, cacheFieldName, cacheType);
            } else {
                m.getField(thisClass, cacheFieldName, cacheType);
            }
        }
    }

    /** Since 2.6.2: get hashing strategies for tuple and store into static field */
    private static class InitHashingStratGenerator extends Generator {
        private final Type[] tupleValTypes;
        private final Type[] hashingStrategies;
        private final Type thisClass;
        private final String hsFieldName;

        InitHashingStratGenerator(Type[] tupleValTypes, Type[] hashingStrategies, Type thisClass, String hsFieldName) {
            this.tupleValTypes = tupleValTypes;
            this.hashingStrategies = hashingStrategies;
            this.thisClass = thisClass;
            this.hsFieldName = hsFieldName;
        }

        @Override
        public void generate(MxGeneratorAdapter sim) {
            sim.push(tupleValTypes.length);
            sim.newArray(CodegenHelper.CLASS_TYPE);
            for (int i = 0; i < tupleValTypes.length; i++) {
                sim.dup();
                sim.push(i);
                sim.push(tupleValTypes[i]);
                sim.arrayStore(CodegenHelper.CLASS_TYPE);
            }
            sim.push(hashingStrategies.length);
            sim.newArray(CodegenHelper.CLASS_TYPE);
            for (int i = 0; i < hashingStrategies.length; i++) {
                sim.dup();
                sim.push(i);
                sim.push(hashingStrategies[i]);
                sim.arrayStore(CodegenHelper.CLASS_TYPE);
            }
            sim.invokeStatic(RuntimeTypes.HASHING_STRATEGY_FACTORY_TYPE, RuntimeTypes.CREATE_HASHING_STRATEGIES_METHOD);
            sim.putStatic(thisClass, hsFieldName, RuntimeTypes.HASHING_STRATEGIES_ARRAY_TYPE);
        }
    }
}
