/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxField;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import gnu.trove.map.hash.THashMap;
import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class NullCacheManager implements CacheManager {
    public static final Type LOCK_TYPE = Type.getType(Lock.class);
    public static final Type STATISTICS_TYPE = Type.getType(Statistics.class);
    public static final Type DEPENDENCY_NODE_TYPE = Type.getType(DependencyNode.class);
    public static final String DUMMY_NODE_FIELD_NAME = "DUMMY_NODE";

    private static final Map<Signature, Constructor> EMPTY_CACHE_IMPLEMENTATIONS = new THashMap<Signature, Constructor>();
    private static int id = 0;

    private final Class<?> ownerClass;
    private final CacheDescriptor descriptor;

    private final Cache staticInstance;

    private final Constructor<? extends Cache> constructor;

    public NullCacheManager(Class<?> ownerClass, CacheDescriptor descriptor) {
        this.ownerClass = ownerClass;
        this.descriptor = descriptor;
        constructor = getImplementation(descriptor.getSignature());
        if (descriptor.isStatic()) {
            staticInstance = createCacheInstance(null);
        } else {
            staticInstance = null;
        }
    }

    static synchronized Constructor<? extends Cache> getImplementation(Signature signature) {
        Signature erased = signature.erased();
        Constructor ctor = EMPTY_CACHE_IMPLEMENTATIONS.get(erased);
        if (ctor == null) {
            ctor = createImplementation(erased);
            EMPTY_CACHE_IMPLEMENTATIONS.put(erased, ctor);
        }
        //noinspection unchecked
        return ctor;
    }

    private static synchronized Constructor createImplementation(Signature signature) {
        String name = Type.getInternalName(NullCacheManager.class) + "$" + signature.getCacheInterface().getSimpleName() + "Impl$" + id++;
        Type cacheType = Type.getType(signature.getCacheInterface());
        ClassGenerator cw = new ClassGenerator(ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC | ACC_FINAL, name, OBJECT_TYPE, cacheType);

        Type calculableType = Type.getType(signature.getCalculableInterface());

        MxField owner = cw.defineField(ACC_PRIVATE | ACC_FINAL, "owner", OBJECT_TYPE);
        MxField calculable = cw.defineField(ACC_PRIVATE | ACC_FINAL, "calculable", calculableType);

        Type valueType = Type.getType(signature.getValue());

        MxConstructorGenerator ctor = cw.defineConstructor(ACC_PUBLIC, OBJECT_TYPE, calculableType);
        ctor.callSuper();
        ctor.initFields(owner, calculable);
        ctor.returnValue();
        ctor.endMethod();

        Type keyType = signature.getContainerType();
        MxGeneratorAdapter getOrCreate = cw.defineMethod(ACC_PUBLIC, new Method("getOrCreate", valueType, signature.getContainer() == null ? EMPTY_TYPES : new Type[] { keyType }));
        getOrCreate.visitCode();
        getOrCreate.get(calculable);
        getOrCreate.get(owner);
        if (keyType != null) {
            getOrCreate.loadArg(0);
        }
        getOrCreate.invokeInterface(calculableType, new Method("calculate", valueType, keyType == null ? new Type[] {OBJECT_TYPE} : new Type[] {OBJECT_TYPE, keyType}));
        getOrCreate.returnValue();
        getOrCreate.endMethod();

        MxGeneratorAdapter getOwner = cw.defineMethod(ACC_PUBLIC, new Method("getCacheOwner", OBJECT_TYPE, EMPTY_TYPES));
        getOwner.visitCode();
        getOwner.get(owner);
        getOwner.returnValue();
        getOwner.endMethod();

        MxGeneratorAdapter clear = cw.defineMethod(ACC_PUBLIC, "invalidate", Type.VOID_TYPE);
        clear.visitCode();
        clear.returnValue();
        clear.endMethod();

        MxGeneratorAdapter getLock = cw.defineMethod(ACC_PUBLIC, "getLock", LOCK_TYPE);
        getLock.visitCode();
        getLock.pushNull();
        getLock.returnValue();
        getLock.endMethod();

        MxGeneratorAdapter size = cw.defineMethod(ACC_PUBLIC, "getSize", Type.INT_TYPE);
        size.visitCode();
        size.push(0);
        size.returnValue();
        size.endMethod();

        MxGeneratorAdapter getStatistics = cw.defineMethod(ACC_PUBLIC, "getStatistics", STATISTICS_TYPE);
        getStatistics.visitCode();
        getStatistics.pushNull();
        getStatistics.returnValue();
        getStatistics.endMethod();

        MxGeneratorAdapter getDependencyNode = cw.defineMethod(ACC_PUBLIC, "getDependencyNode", DEPENDENCY_NODE_TYPE);
        getDependencyNode.visitCode();
        getDependencyNode.getStatic(Type.getType(DependencyTracker.class), DUMMY_NODE_FIELD_NAME, DEPENDENCY_NODE_TYPE);
        getDependencyNode.returnValue();
        getDependencyNode.endMethod();

        Class cls = cw.toClass(NullCacheManager.class.getClassLoader());
        try {
            return cls.getConstructor(Object.class, signature.getCalculableInterface());
        } catch (NoSuchMethodException e) {
            throw new MxCacheException(e);
        }
    }

    private Cache createCacheInstance(@Nullable Object t) {
        try {
            return constructor.newInstance(t, descriptor.getCalculable());
        } catch (InstantiationException e) {
            throw new MxCacheException(e);
        } catch (IllegalAccessException e) {
            throw new MxCacheException(e);
        } catch (InvocationTargetException e) {
            throw new MxCacheException(e);
        }
    }

    @Override
    public CacheDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public Cache createCache(@Nullable Object owner) {
        return staticInstance == null ? createCacheInstance(owner) : staticInstance;
    }

    @Override
    public String getImplementationDetails() {
        return "<cache disabled>";
    }

    @Override
    public CacheContext getContext() {
        return null;
    }

    @Override
    public Class<?> getOwnerClass() {
        return ownerClass;
    }
}
