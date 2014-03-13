/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.transform.TransformGenerator;
import com.maxifier.mxcache.transform.TransformGeneratorFactory;
import com.maxifier.mxcache.transform.TransformGeneratorFactoryImpl;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.lang.annotation.Annotation;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.getType;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ResolvableGenerator {
    public static final Type RESOLVABLE_TYPE = getType(Resolvable.class);
    private static final Type CACHE_CONTEXT_TYPE = getType(CacheContext.class);

    private static final Method GET_VALUE_METHOD = Method.getMethod("Object getValue()");

    private static final String OWNER_FIELD_NAME = "owner";

    public static final String NON_PROXIED_POSTFIX = "$nonproxied$";

    private ResolvableGenerator() {
    }

    public static void generateResolvable(Class ownerClass, int id, String name, String desc, boolean isStatic) {
        generateResolvable(ownerClass, id, new Method(name, desc), isStatic);
    }

    public static void generateResolvable(Class ownerClass, int id, Method method, boolean isStatic) {
        Method nonProxiedMethod = new Method(getNonProxiedMethodName(method), method.getDescriptor());
        generateResolvable(ownerClass.getClassLoader(), getResolvableName(id, Type.getType(ownerClass)), ownerClass, nonProxiedMethod, nonProxiedMethod.getArgumentTypes(), isStatic, getKeyTransformers(ownerClass, method));
    }

    public static void generateResolvable(ClassLoader classLoader, String resolvableName, Class ownerClass, Method nonProxiedMethod, Type[] genericArguments, boolean isStatic, TransformGenerator[] tg) {
        Type ownerType = Type.getType(ownerClass);

        ClassGenerator w = new ClassGenerator(ACC_SUPER, resolvableName,  OBJECT_TYPE, RESOLVABLE_TYPE);

        Type resolvableType = w.getThisType();

        if (tg != null) {
            int index = 0;
            for (TransformGenerator generator : tg) {
                generator.generateFields(resolvableType, index, w);
                index += generator.getFieldCount();
            }
        }

        Type[] arguments = nonProxiedMethod.getArgumentTypes();
        if (!isStatic) {
            w.defineField(ACC_PRIVATE | ACC_FINAL, OWNER_FIELD_NAME, ownerType);
        }
        Type[] stored = new Type[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Type arg = genericArguments[i];
            if (tg != null) {
                stored[i] = Type.getType(tg[i].getTransformedType(toClass(classLoader, arg)));
            } else {
                stored[i] = arg;
            }
            w.defineField(ACC_PRIVATE | ACC_FINAL, "arg" + i, stored[i]);
        }

        Type[] ctorArguments = isStatic ? arguments : CodegenHelper.insertFirst(arguments, ownerType);
        MxConstructorGenerator ctor = w.defineConstructor(ACC_PUBLIC, ctorArguments);
        generateResolvableCtor(ownerType, resolvableType, arguments, genericArguments, ctor, isStatic, tg, stored);

        MxGeneratorAdapter getValue = w.defineMethod(ACC_PUBLIC, GET_VALUE_METHOD);
        generateGetValue(ownerClass, ownerType, nonProxiedMethod, isStatic, resolvableType, arguments, getValue, tg, stored);

        w.toClass(classLoader);
    }

    private static TransformGenerator[] getKeyTransformers(Class ownerClass, Method method) {
        TransformGeneratorFactory tgf = TransformGeneratorFactoryImpl.getInstance();

        Class[] params = CodegenHelper.getClasses(ownerClass.getClassLoader(), method.getArgumentTypes());
        Annotation[][] annotations;
        try {
            annotations = ownerClass.getDeclaredMethod(method.getName(), params).getParameterAnnotations();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }

        TransformGenerator[] tg = new TransformGenerator[params.length];
        for (int i = 0; i<params.length; i++) {
            tg[i] = tgf.forArgument(annotations[i], params[i]);
        }
        return tg;
    }

    public static String getResolvableName(int id, Type ownerType) {
        return ownerType.getInternalName() + "$Resolvable" + id;
    }

    private static void generateGetValue(Class owner, Type ownerType, Method method, boolean isStatic, Type resolvableType, Type[] arguments, MxGeneratorAdapter getValue, TransformGenerator[] tg, Type[] stored) {
        getValue.visitCode();

        if (!isStatic) {
            getValue.loadThis();
            getValue.getField(resolvableType, OWNER_FIELD_NAME, ownerType);
        }
        int index = 0;
        for (int i = 0; i < arguments.length; i++) {
            getValue.loadThis();
            getValue.getField(resolvableType, "arg" + i, stored[i]);
            if (tg != null) {
                tg[i].generateBackward(resolvableType, index, getValue);
                index += tg[i].getFieldCount();
            }
        }

        getValue.visitMethodInsn(isStatic ? INVOKESTATIC : owner.isInterface() ? INVOKEINTERFACE : INVOKEVIRTUAL, ownerType.getInternalName(), method.getName(), method.getDescriptor());
        getValue.returnValue();
        getValue.endMethod();
    }

    private static void generateResolvableCtor(Type ownerType, Type resolvableType, Type[] arguments, Type[] genericArguments, MxConstructorGenerator ctor, boolean isStatic, TransformGenerator[] tg, Type[] stored) {
        ctor.callSuper();

        if (tg != null) {
            int contextLocal = ctor.newLocal(CACHE_CONTEXT_TYPE);

            ctor.invokeStatic(Type.getType(CacheFactory.class), new Method("getDefaultContext", CACHE_CONTEXT_TYPE, EMPTY_TYPES));
            ctor.storeLocal(contextLocal);

            int index = 0;
            for (TransformGenerator generator : tg) {
                generator.generateAcquire(resolvableType, index, ctor, contextLocal);
                index += generator.getFieldCount();
            }
        }

        if (!isStatic) {
            ctor.loadThis();
            ctor.loadArg(0);
            ctor.putField(resolvableType, OWNER_FIELD_NAME, ownerType);
        }
        int index = 0;
        for (int i = 0; i < arguments.length; i++) {
            ctor.loadThis();
            ctor.loadArg(isStatic ? i : i + 1);
            if (!genericArguments[i].equals(arguments[i])) {
                ctor.checkCast(genericArguments[i]);
            }
            if (tg != null) {
                tg[i].generateForward(resolvableType, index, ctor);
                index += tg[i].getFieldCount();
            }
            ctor.putField(resolvableType, "arg" + i, stored[i]);
        }
        ctor.returnValue();
        ctor.endMethod();
    }

    public static String getNonProxiedMethodName(Method m) {
        return NON_PROXIED_POSTFIX + m.getName();
    }
}
