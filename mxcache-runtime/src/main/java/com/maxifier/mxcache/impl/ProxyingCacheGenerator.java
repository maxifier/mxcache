/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.proxy.ResolvableGenerator;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.proxy.ProxyFactory;
import com.maxifier.mxcache.transform.TransformGenerator;
import com.maxifier.mxcache.proxy.Resolvable;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxField;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ProxyingCacheGenerator {
    private static final Type STATISTICS_TYPE = getType(Statistics.class);
    private static final Type PROXY_FACTORY_TYPE = getType(ProxyFactory.class);
    private static final Type RESOLVABLE_TYPE = getType(Resolvable.class);
    private static final Type LOCK_TYPE = Type.getType(Lock.class);
    private static final Type DEPENDENCY_NODE_TYPE = Type.getType(DependencyNode.class);

    private static final String CACHE_FIELD = "cache";
    private static final String PROXY_FACTORY_FIELD = "proxyFactory";
    private static final String DUMMY_NODE_FIELD_NAME = "DUMMY_NODE";

    private static final Method GET_STATISTICS_METHOD = new Method("getStatistics", STATISTICS_TYPE, EMPTY_TYPES);
    private static final Method SIZE_METHOD = new Method("getSize", INT_TYPE, EMPTY_TYPES);
    private static final Method OWNER_METHOD = new Method("getCacheOwner", OBJECT_TYPE, EMPTY_TYPES);
    private static final Method GET_LOCK_METHOD = new Method("getLock", LOCK_TYPE, EMPTY_TYPES);
    private static final Method CLEAR_METHOD = Method.getMethod("void clear()");
    private static final Method PROXY_METHOD = new Method("proxy", OBJECT_TYPE, new Type[] { CLASS_TYPE, RESOLVABLE_TYPE });

    private static final String GET_OR_CREATE = "getOrCreate";

    private static final AtomicInteger PROXYING_CALCULATABLE_ID = new AtomicInteger();

    private ProxyingCacheGenerator() {
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T wrapCacheWithProxy(CacheDescriptor descriptor, CacheContext context, T cache) {
        ProxyFactory proxyFactory = descriptor.getProxyFactory(context);
        Class key = descriptor.getKeyType();
        Class value = descriptor.getValueType();
        Class<T> cacheInterface = descriptor.getCacheInterface();
        TransformGenerator keyTransform = descriptor.getKeyTransform();
        ClassLoader owner = descriptor.getOwnerClass().getClassLoader();
        return wrapCacheWithProxy(owner, cache, proxyFactory, key, value, cacheInterface, keyTransform);
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T wrapCacheWithProxy(ClassLoader owner, T cache, ProxyFactory proxyFactory, Class key, Class value, Class<T> cacheInterface, TransformGenerator keyTransform) {
        if (proxyFactory == null) {
            return cache;
        }
        Class calculatableClass = generateProxyingCacheClass(owner, key, value, cacheInterface, keyTransform);
        try {
            return (T) calculatableClass.getConstructors()[0].newInstance(cache, proxyFactory);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Invalid calculatable generated", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Invalid calculatable generated", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Invalid calculatable generated", e);
        }
    }

    private static byte[] generateProxyingCacheBytecode(ClassLoader owner, Class key, Class value, Class cacheInterface, TransformGenerator keyTransform) {
        if (value.isPrimitive()) {
            throw new UnsupportedOperationException("Cannot proxy primitive!");
        }
        Type ekey = key == null ? null : erase(Type.getType(key));

        int id = PROXYING_CALCULATABLE_ID.getAndIncrement();

        String packageName = StorageBasedCacheManager.class.getPackage().getName().replace('.', '/');
        String thisName = packageName + "/ProxyingCache$" + id;
        String calculatableName = packageName + "/ProxyingCache$" + id + "$Calculatable";

        Type calculatableType = Type.getObjectType(calculatableName);
        Type cacheType = getType(cacheInterface);

        ClassGenerator writer = new ClassGenerator(ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, thisName, OBJECT_TYPE, cacheType);

        MxField cacheField = writer.defineField(ACC_PRIVATE | ACC_FINAL, CACHE_FIELD, cacheType);
        MxField proxyFactoryField = writer.defineField(ACC_PRIVATE | ACC_FINAL, PROXY_FACTORY_FIELD, PROXY_FACTORY_TYPE);

        generateGetDependencyNode(writer);

        generateSize(writer, cacheField);

        generateOwner(writer, cacheField);

        generateGetStatistics(writer, cacheField);

        generateCtor(writer, cacheField, proxyFactoryField);

        generateGetOrCreate(key, value, writer, calculatableType, ekey, cacheField, proxyFactoryField);

        generateGetLock(writer, cacheField);

        generateClear(writer, cacheField);

        writer.visitEnd();

        Type[] keyTypes = key == null ? EMPTY_TYPES : new Type[]{getType(key)};
        Type[] ekeyTypes = ekey == null ? EMPTY_TYPES : new Type[]{ekey};
        TransformGenerator[] transforms = keyTransform == null ? null : new TransformGenerator[]{keyTransform};
        ResolvableGenerator.generateResolvable(owner, calculatableName, cacheInterface, new Method("getOrCreate", OBJECT_TYPE, ekeyTypes), keyTypes, false, transforms);
        return writer.toByteArray();
    }

    private static void generateGetDependencyNode(ClassGenerator writer) {
        MxGeneratorAdapter getDependencyNode = writer.defineMethod(ACC_PUBLIC, "getDependencyNode", DEPENDENCY_NODE_TYPE);
        getDependencyNode.visitCode();
        getDependencyNode.getStatic(Type.getType(DependencyTracker.class), DUMMY_NODE_FIELD_NAME, DEPENDENCY_NODE_TYPE);
        getDependencyNode.returnValue();
        getDependencyNode.endMethod();
    }

    private static void generateSize(ClassGenerator writer, MxField cacheField) {
        MxGeneratorAdapter size = writer.defineMethod(ACC_PUBLIC, SIZE_METHOD);
        size.visitCode();
        size.get(cacheField);
        size.invokeInterface(cacheField.getType(), SIZE_METHOD);
        size.returnValue();
        size.endMethod();
    }

    private static void generateOwner(ClassGenerator writer, MxField cacheField) {
        MxGeneratorAdapter size = writer.defineMethod(ACC_PUBLIC, OWNER_METHOD);
        size.visitCode();
        size.get(cacheField);
        size.invokeInterface(cacheField.getType(), OWNER_METHOD);
        size.returnValue();
        size.endMethod();
    }

    private static void generateGetStatistics(ClassGenerator writer, MxField cacheField) {
        MxGeneratorAdapter size = writer.defineMethod(ACC_PUBLIC, GET_STATISTICS_METHOD);
        size.visitCode();
        size.get(cacheField);
        size.invokeInterface(cacheField.getType(), GET_STATISTICS_METHOD);
        size.returnValue();
        size.endMethod();
    }

    private static Class generateProxyingCacheClass(ClassLoader owner, Class key, Class value, Class cacheInterface, TransformGenerator keyTransform) {
        byte[] bytecode = generateProxyingCacheBytecode(owner, key, value, cacheInterface, keyTransform);

        return loadClass(owner, bytecode);
    }

    private static void generateClear(ClassGenerator writer, MxField cacheField) {
        MxGeneratorAdapter clear = writer.defineMethod(ACC_PUBLIC, CLEAR_METHOD);
        clear.visitCode();
        clear.get(cacheField);
        clear.invokeInterface(cacheField.getType(), CLEAR_METHOD);
        clear.returnValue();
        clear.endMethod();
    }

    private static void generateGetLock(ClassGenerator writer, MxField cacheField) {
        MxGeneratorAdapter getLock = writer.defineMethod(ACC_PUBLIC, GET_LOCK_METHOD);
        getLock.visitCode();
        getLock.get(cacheField);
        getLock.invokeInterface(cacheField.getType(), GET_LOCK_METHOD);
        getLock.returnValue();
        getLock.endMethod();
    }

    private static void generateCtor(ClassGenerator writer, MxField cacheField, MxField proxyFactoryField) {
        MxConstructorGenerator ctor = writer.defineConstructor(ACC_PUBLIC, cacheField.getType(), proxyFactoryField.getType());
        ctor.callSuper();
        ctor.initFields(cacheField, proxyFactoryField);
        ctor.returnValue();
        ctor.endMethod();
    }

    private static void generateGetOrCreate(Class key, Class value, ClassGenerator writer, Type calculatableType, Type ekey, MxField cacheField, MxField proxyFactoryField) {
        MxGeneratorAdapter calculate = writer.defineMethod(ACC_PUBLIC, GET_OR_CREATE, OBJECT_TYPE, ekey == null ? EMPTY_TYPES : new Type[] { ekey });
        calculate.visitCode();
        calculate.get(proxyFactoryField);
        calculate.push(Type.getType(value));
        calculate.newInstance(calculatableType);
        calculate.dup();
        calculate.get(cacheField);
        if (key != null) {
            calculate.loadArg(0);
            if (!key.isPrimitive()) {
                calculate.checkCast(getType(key));
            }
        }
        calculate.invokeConstructor(calculatableType, new Method(CONSTRUCTOR_NAME, VOID_TYPE, ekey == null ? new Type[] { cacheField.getType() } : new Type[] { cacheField.getType(), ekey}));
        calculate.invokeInterface(PROXY_FACTORY_TYPE, PROXY_METHOD);
        calculate.returnValue();
        calculate.endMethod();
    }
}
