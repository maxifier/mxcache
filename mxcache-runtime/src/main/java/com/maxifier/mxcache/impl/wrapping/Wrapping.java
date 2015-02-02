/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.ObjectObjectCache;
import com.maxifier.mxcache.caches.ObjectObjectCalculatable;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.caches.storage.StorageHolder;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;
import com.maxifier.mxcache.impl.resource.nodes.MultipleDependencyNode;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.transform.*;
import com.maxifier.mxcache.tuple.*;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.CodegenHelper;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxField;
import com.maxifier.mxcache.util.MxGeneratorAdapter;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.VOID_TYPE;
import static com.maxifier.mxcache.transform.ChainedTransformGenerator.*;
import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class Wrapping {
    private static final Type MUTABLE_STATISTICS_TYPE = Type.getType(MutableStatistics.class);
    private static final Type STORAGE_TYPE = Type.getType(Storage.class);
    private static final Type STATISTICS_HOLDER_TYPE = Type.getType(StatisticsHolder.class);
    private static final Type STORAGE_HOLDER_TYPE = Type.getType(StorageHolder.class);
    private static final Type LOCK_TYPE = Type.getType(Lock.class);

    private static final String STORAGE_FIELD = "storage";

    private static final String LOAD_METHOD = "load";
    private static final String SAVE_METHOD = "save";
    private static final String LOCK_METHOD = "lock";
    private static final String UNLOCK_METHOD = "unlock";

    private static final Type STATISTICS_TYPE = Type.getType(Statistics.class);
    private static final Method GET_STATISTICS_METHOD = new Method("getStatistics", STATISTICS_TYPE, EMPTY_TYPES);
    private static final Method GET_LOCK_METHOD = new Method("getLock", LOCK_TYPE, EMPTY_TYPES);
    private static final Method SIZE_METHOD = Method.getMethod("int size()");
    private static final Method CLEAR_METHOD = Method.getMethod("void clear()");

    private static final AtomicLong WRAPPER_ID = new AtomicLong();

    private static final Method SET_STORAGE_TYPE = new Method("setStorage", VOID_TYPE, new Type[]{STORAGE_TYPE});
    private static final Type UNSUPPORTED_OPERATION_EXCEPTION_TYPE = Type.getType(UnsupportedOperationException.class);
    private static final TupleFactory TUPLE_FACTORY = TupleGenerator.getTupleFactory(Signature.class, boolean.class);
    private static final String FULL_LOCKED_PACKAGE = "com.maxifier.mxcache.impl.caches.storage";
    private static final String ELEMENT_LOCKED_PACKAGE = "com.maxifier.mxcache.impl.caches.storage.elementlocked";
    private static final String NODES_PACKAGE = "com.maxifier.mxcache.impl.resource.nodes";

    private Wrapping() {
    }

    private static final Map<Tuple, WrapperFactory> PLAIN_CACHE = new THashMap<Tuple, WrapperFactory>();
    private static final Map<CacheImplementationSignature, WrapperFactory> CONVERTING_CACHE = new THashMap<CacheImplementationSignature, WrapperFactory>();

    private static final Map<Signature, Constructor<Cache>> CACHE_WRAPPER_CONSTRUCTORS_CACHE =
            new THashMap<Signature, Constructor<Cache>>();
    private static final Map<Signature, Constructor<ObjectObjectCalculatable>> CALCULABLE_WRAPPER_CONSTRUCTORS_CACHE =
            new THashMap<Signature, Constructor<ObjectObjectCalculatable>>();

    public static WrapperFactory getFactory(Signature storageSignature, Signature cacheSignature, boolean perElementLocking) {
        return getFactory(storageSignature, cacheSignature, null, null, perElementLocking);
    }

    public static WrapperFactory getFactory(Signature storageSignature, Signature cacheSignature, @Nullable TransformGenerator userKeyTransformer, @Nullable TransformGenerator userValueTransformer, boolean perElementLocking) {
        if (isNoTransform(userKeyTransformer) && isNoTransform(userValueTransformer) && storageSignature.isWider(cacheSignature)) {
            return getPlainFactory(storageSignature, perElementLocking);
        }
        return getConvertingFactory(storageSignature, cacheSignature, wrapNullTransform(cacheSignature.getContainer(), userKeyTransformer), wrapNullTransform(cacheSignature.getValue(), userValueTransformer), perElementLocking);
    }

    private static boolean isNoTransform(TransformGenerator userKeyTransformer) {
        return (userKeyTransformer == null || userKeyTransformer instanceof EmptyTransformGenerator);
    }

    private static synchronized WrapperFactory getPlainFactory(Signature signature, boolean perElementLocking) {
        Tuple key = TUPLE_FACTORY.create(signature, perElementLocking);
        WrapperFactory factory = PLAIN_CACHE.get(key);
        if (factory == null) {
            factory = createPlainFactory(signature, perElementLocking);
            PLAIN_CACHE.put(key, factory);
        }
        return factory;
    }

    private static synchronized WrapperFactory getConvertingFactory(Signature storageSignature, Signature cacheSignature, @Nonnull TransformGenerator userKeyTransformer, @Nonnull TransformGenerator userValueTransformer, boolean perElementLocking) {
        CacheImplementationSignature signature = new CacheImplementationSignature(
                cacheSignature.getCacheInterface(),
                cacheSignature.getCalculableInterface(),
                perElementLocking ? storageSignature.getElementLockedStorageInterface() : storageSignature.getStorageInterface(),
                userKeyTransformer,
                userValueTransformer,
                perElementLocking);
        WrapperFactory factory = CONVERTING_CACHE.get(signature);
        if (factory == null) {
            factory = createConvertingFactory(storageSignature, cacheSignature, signature, userKeyTransformer, userValueTransformer, perElementLocking);
            CONVERTING_CACHE.put(signature, factory);
        }
        return factory;
    }

    private static TransformGenerator wrapNullTransform(Class<?> expectedType, TransformGenerator transform) {
        return transform == null ? new EmptyTransformGenerator(expectedType) : transform;
    }

    private static WrapperFactory createPlainFactory(Signature signature, boolean perElementLocking) {
        return new WrapperFactoryImpl(getOnlyConstructor(getCacheImplClass(signature, perElementLocking)));
    }

    public static AbstractDependencyNode getSingletonNode(CacheDescriptor descriptor) {
        Constructor<? extends SingletonDependencyNode> onlyConstructor = null;
        if (descriptor.isResourceView() && descriptor.getKeyType() == null) {
            onlyConstructor = getOnlyConstructor(getSingletonDependencyNodeClass(descriptor.getSignature()));
        }
        return onlyConstructor == null ? new SingletonDependencyNode() : new NodeWrapperFactoryImpl(onlyConstructor).wrap();
    }

    public static AbstractDependencyNode getMultipleNode(CacheDescriptor descriptor) {
        Constructor<? extends MultipleDependencyNode> onlyConstructor = null;
        if (descriptor.isResourceView() && descriptor.getKeyType() == null) {
            onlyConstructor = getOnlyConstructor(getMultipleDependencyNodeClass(descriptor.getSignature()));
        }
        return onlyConstructor == null ? new MultipleDependencyNode() : new NodeWrapperFactoryImpl(onlyConstructor).wrap();
    }

    private static WrapperFactory createConvertingFactory(Signature storageSignature, Signature cacheSignature, CacheImplementationSignature signature, @Nonnull TransformGenerator userKeyTransformer, @Nonnull TransformGenerator userValueTransformer, boolean perElementLocking) {
        @Nonnull
        TransformGenerator keyTransformer = boxTransformer(userKeyTransformer, cacheSignature.getContainer(), storageSignature.getContainer());
        @Nonnull
        TransformGenerator valueTransformer = wrapUndefined(boxTransformer(userValueTransformer, Object.class, Object.class));

        byte[] bytecode = generateWrapperBytecode(storageSignature, cacheSignature, keyTransformer, valueTransformer, signature, perElementLocking);

        Class<? extends Cache> wrapperClass = loadClass(Wrapping.class.getClassLoader(), bytecode);

        return new WrapperFactoryImpl(getOnlyConstructor(wrapperClass));
    }

    private static TransformGenerator wrapUndefined(final TransformGenerator transformGenerator) {
        if (transformGenerator instanceof EmptyTransformGenerator) {
            return transformGenerator;
        }
        return new TransformGenerator() {
            @Override
            public void generateForward(Type thisType, int fieldIndex, GeneratorAdapter method) {
                method.dup();
                method.getStatic(STORAGE_TYPE, "UNDEFINED", OBJECT_TYPE);
                Label end = new Label();
                method.ifCmp(OBJECT_TYPE, GeneratorAdapter.EQ, end);
                transformGenerator.generateForward(thisType, fieldIndex, method);
                method.mark(end);
            }

            @Override
            public void generateBackward(Type thisType, int fieldIndex, GeneratorAdapter method) {
                method.dup();
                method.getStatic(STORAGE_TYPE, "UNDEFINED", OBJECT_TYPE);
                Label end = new Label();
                method.ifCmp(OBJECT_TYPE, GeneratorAdapter.EQ, end);
                transformGenerator.generateBackward(thisType, fieldIndex, method);
                method.mark(end);
            }

            @Override
            public void generateFields(Type thisType, int fieldIndex, ClassGenerator writer) {
                transformGenerator.generateFields(thisType, fieldIndex, writer);
            }

            @Override
            public void generateAcquire(Type thisType, int fieldIndex, GeneratorAdapter ctor, int contextLocal) {
                transformGenerator.generateAcquire(thisType, fieldIndex, ctor, contextLocal);
            }

            @Override
            public int getFieldCount() {
                return transformGenerator.getFieldCount();
            }

            @Override
            public Signature transformKey(Signature in) {
                return transformGenerator.transformKey(in);
            }

            @Override
            public Signature transformValue(Signature in) {
                return transformGenerator.transformValue(in);
            }

            @Override
            public Class<?> getInType() {
                return Object.class;
            }

            @Override
            public Class<?> getOutType() {
                return Object.class;
            }
        };
    }

    @Nonnull
    private static TransformGenerator boxTransformer(TransformGenerator transformer, Class from, Class to) {
        if (transformer instanceof EmptyTransformGenerator) {
            return getBoxingTransformGenerator(from, to);
        }
        return chain(chain(getBoxingTransformGenerator(from, transformer.getInType()), transformer), getBoxingTransformGenerator(transformer.getOutType(), to));
    }

    private static byte[] generateWrapperBytecode(Signature storageSignature, Signature cacheSignature, @Nonnull TransformGenerator keyTransformer, @Nonnull TransformGenerator valueTransformer, CacheImplementationSignature signature, boolean perElementLocking) {
        String superName = cacheSignature.getImplementationClassName(getAbstractCachePackage(perElementLocking) + "/Abstract", "Cache");
        String className = getCacheImplClassName(storageSignature, cacheSignature);

        ClassGenerator w = new ClassGenerator(ACC_PUBLIC | ACC_SUPER, className, Type.getObjectType(superName), STORAGE_HOLDER_TYPE);

        Type wrapperType = Type.getObjectType(className);

        Type calculatableType = Type.getType(signature.getCalculatableClass());
        Type storageType = Type.getType(signature.getStorageClass());

        w.defineField(ACC_PRIVATE, STORAGE_FIELD, storageType);
        keyTransformer.generateFields(wrapperType, 0, w);
        valueTransformer.generateFields(wrapperType, keyTransformer.getFieldCount(), w);

        generateSetStorage(w, wrapperType, storageType);

        generateConstructor(w, Type.getObjectType(superName), calculatableType);

        generateDelegatingMethod(w, wrapperType, storageType, SIZE_METHOD);
        generateDelegatingMethod(w, wrapperType, storageType, CLEAR_METHOD);

        generateGetStatistics(w, superName, wrapperType, storageType);

        Type erasedCacheKeyType = erase(cacheSignature.getContainerType());
        Type storageKeyType = storageSignature.getContainerType();

        generateLoad(keyTransformer, valueTransformer, w, wrapperType, storageType, erasedCacheKeyType, storageKeyType);

        generateSave(keyTransformer, valueTransformer, w, wrapperType, storageType, erasedCacheKeyType, storageKeyType);

        if (perElementLocking) {
            generateDelegatingLockingMethod(LOCK_METHOD, keyTransformer, w, wrapperType, storageType, erasedCacheKeyType, storageKeyType);
            generateDelegatingLockingMethod(UNLOCK_METHOD, keyTransformer, w, wrapperType, storageType, erasedCacheKeyType, storageKeyType);
            generateGetLock(w, wrapperType, storageType);

        }

        w.visitEnd();
        return w.toByteArray();
    }

    private static String getAbstractCachePackage(boolean perElementLocking) {
        return perElementLocking ? "com/maxifier/mxcache/impl/caches/abs/elementlocked" : "com/maxifier/mxcache/impl/caches/abs";
    }

    private static void generateSetStorage(ClassVisitor w, Type wrapperType, Type storageType) {
        WrapperMethodGenerator setStorage = defineMethod(w, SET_STORAGE_TYPE);
        setStorage.visitCode();

        Label save = new Label();

        setStorage.loadThis();
        setStorage.getField(wrapperType, STORAGE_FIELD, storageType);
        setStorage.ifNull(save);

        setStorage.throwException(UNSUPPORTED_OPERATION_EXCEPTION_TYPE, "Storage already set");

        setStorage.mark(save);
        setStorage.loadThis();
        setStorage.loadArg(0);
        setStorage.checkCast(storageType);
        setStorage.putField(wrapperType, STORAGE_FIELD, storageType);

        setStorage.returnValue();
        setStorage.endMethod();
    }

    private static void generateGetStatistics(ClassVisitor w, String superName, Type wrapperType, Type storageType) {
        WrapperMethodGenerator getStatistics = defineMethod(w, GET_STATISTICS_METHOD);
        getStatistics.loadStorage(wrapperType, storageType);
        getStatistics.instanceOf(STATISTICS_HOLDER_TYPE);
        Label delegating = new Label();
        getStatistics.ifZCmp(GeneratorAdapter.EQ, delegating);
        getStatistics.loadStorage(wrapperType, storageType);
        getStatistics.checkCast(STATISTICS_HOLDER_TYPE);
        getStatistics.invokeInterface(STATISTICS_HOLDER_TYPE, GET_STATISTICS_METHOD);
        getStatistics.returnValue();

        getStatistics.mark(delegating);

        getStatistics.loadThis();
        getStatistics.invokeConstructor(Type.getObjectType(superName), GET_STATISTICS_METHOD);
        getStatistics.returnValue();
        getStatistics.endMethod();
    }

    private static void generateSave(@Nonnull TransformGenerator keyTransformer, @Nonnull TransformGenerator valueTransformer, ClassVisitor w, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type storageKeyType) {
        Method cacheSaveMethod = new Method(SAVE_METHOD, VOID_TYPE, erasedCacheKeyType == null ? new Type[]{CodegenHelper.OBJECT_TYPE} : new Type[]{erasedCacheKeyType, CodegenHelper.OBJECT_TYPE});
        Method storageSaveMethod = new Method(SAVE_METHOD, VOID_TYPE, storageKeyType == null ? new Type[]{CodegenHelper.OBJECT_TYPE} : new Type[]{erase(storageKeyType), CodegenHelper.OBJECT_TYPE});
        WrapperMethodGenerator save = defineMethod(w, cacheSaveMethod);
        save.visitCode();

        save.loadStorage(wrapperType, storageType);

        if (erasedCacheKeyType != null) {
            save.loadArg(0);
            keyTransformer.generateForward(wrapperType, 0, save);
        }
        save.loadArg(erasedCacheKeyType == null ? 0 : 1);
        valueTransformer.generateForward(wrapperType, keyTransformer.getFieldCount(), save);
        save.invokeInterface(storageType, storageSaveMethod);
        save.returnValue();

        save.endMethod();
    }

    private static void generateDelegatingLockingMethod(String name, @Nonnull TransformGenerator keyTransformer, ClassVisitor w, Type wrapperType, Type storageType, @Nonnull Type erasedCacheKeyType, @Nonnull Type storageKeyType) {
        Method cacheLockMethod = new Method(name, VOID_TYPE, new Type[]{erasedCacheKeyType});
        Method storageLockMethod = new Method(name, VOID_TYPE, new Type[]{erase(storageKeyType)});
        WrapperMethodGenerator save = defineMethod(w, cacheLockMethod);

        save.visitCode();
        save.loadStorage(wrapperType, storageType);

        save.loadArg(0);
        keyTransformer.generateForward(wrapperType, 0, save);
        save.invokeInterface(storageType, storageLockMethod);
        save.returnValue();

        save.endMethod();
    }

    private static void generateGetLock(ClassVisitor w, Type wrapperType, Type storageType) {
        WrapperMethodGenerator save = defineMethod(w, GET_LOCK_METHOD);

        save.visitCode();
        save.loadStorage(wrapperType, storageType);
        save.invokeInterface(storageType, GET_LOCK_METHOD);
        save.returnValue();

        save.endMethod();
    }

    private static void generateDelegatingMethod(ClassVisitor w, Type wrapperType, Type storageType, Method method) {
        WrapperMethodGenerator size = defineMethod(w, method);
        size.loadStorage(wrapperType, storageType);
        size.invokeInterface(storageType, method);
        size.returnValue();
        size.endMethod();
    }

    private static Type[] getArgumentTypes(@Nullable Type type) {
        return type == null ? EMPTY_TYPES : new Type[]{erase(type)};
    }

    private static void generateLoad(@Nonnull TransformGenerator keyTransformer, @Nonnull TransformGenerator valueTransformer, ClassVisitor w, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type storageKeyType) {
        Method cacheLoadMethod = new Method(LOAD_METHOD, CodegenHelper.OBJECT_TYPE, getArgumentTypes(erasedCacheKeyType));
        Method storageLoadMethod = new Method(LOAD_METHOD, CodegenHelper.OBJECT_TYPE, getArgumentTypes(storageKeyType));

        WrapperMethodGenerator load = defineMethod(w, cacheLoadMethod);
        load.loadStorage(wrapperType, storageType);
        if (erasedCacheKeyType != null) {
            load.loadArg(0);
            keyTransformer.generateForward(wrapperType, 0, load);
        }
        load.invokeInterface(storageType, storageLoadMethod);
        valueTransformer.generateBackward(wrapperType, keyTransformer.getFieldCount(), load);
        load.returnValue();

        load.endMethod();
    }

    private static void generateConstructor(ClassVisitor w, Type superType, Type calculatableType) {
        Method method = new Method(CONSTRUCTOR_NAME, VOID_TYPE, new Type[]{OBJECT_TYPE, calculatableType, MUTABLE_STATISTICS_TYPE});

        WrapperMethodGenerator ctor = defineMethod(w, method);
        ctor.visitCode();
        ctor.loadThis();
        ctor.loadArg(0);
        ctor.loadArg(1);
        ctor.loadArg(2);
        ctor.invokeConstructor(superType, method);
        ctor.returnValue();
        ctor.endMethod();
    }

    private static WrapperMethodGenerator defineMethod(ClassVisitor w, Method ctorMethod) {
        return new WrapperMethodGenerator(ACC_PUBLIC, ctorMethod, w.visitMethod(ACC_PUBLIC, ctorMethod.getName(), ctorMethod.getDescriptor(), null, null));
    }

    private static String getCacheImplClassName(Signature storageSignature, Signature cacheSignature) {
        String extension = storageSignature.getImplementationClassName("$", "CacheImpl$" + WRAPPER_ID.getAndIncrement());
        return cacheSignature.getImplementationClassName("com/maxifier/mxcache/impl/caches/wrapping/Storage", extension);
    }

    @Nonnull
    private static TransformGenerator getBoxingTransformGenerator(Class<?> from, Class<?> to) {
        if (from == to) {
            return new EmptyTransformGenerator(from);
        }
        if (from != null && to != null) {
            if (to.isAssignableFrom(from)) {
                return new EmptyTransformGenerator(from);
            }
            if (to.isPrimitive() && !from.isPrimitive() && from.isAssignableFrom(getBoxedType(to))) {
                return new UnboxTransformGenerator(to);
            }
            if (from.isPrimitive() && !to.isPrimitive() && to.isAssignableFrom(getBoxedType(from))) {
                return new BoxTransformGenerator(from, Type.getType(from));
            }
        }
        throw new UnsupportedOperationException("Cannot convert from " + from + " to " + to);
    }

    @SuppressWarnings({"unchecked"})
    public static Class<? extends Cache> getCacheImplClass(Signature cacheSignature, boolean perElementLocking) {
        return (Class<? extends Cache>) cacheSignature.getImplementationClass(getWrapperPackage(perElementLocking) + ".Storage", "CacheImpl");
    }

    @SuppressWarnings({"unchecked"})
    public static Class<? extends SingletonDependencyNode> getSingletonDependencyNodeClass(Signature nodeSignature) {
        return (Class<? extends SingletonDependencyNode>) nodeSignature.getImplementationClass(NODES_PACKAGE + ".ViewableSingleton", "DependencyNode");
    }

    @SuppressWarnings({"unchecked"})
    public static Class<? extends MultipleDependencyNode> getMultipleDependencyNodeClass(Signature nodeSignature) {
        return (Class<? extends MultipleDependencyNode>) nodeSignature.getImplementationClass(NODES_PACKAGE + ".ViewableMultiple", "DependencyNode");
    }

    private static String getWrapperPackage(boolean perElementLocking) {
        return perElementLocking ? ELEMENT_LOCKED_PACKAGE : FULL_LOCKED_PACKAGE;
    }

    @SuppressWarnings({"unchecked"})
    private static <T> Constructor<? extends T> getOnlyConstructor(Class<? extends T> wrapperClass) {
        Constructor<?>[] constructors = wrapperClass.getConstructors();
        if (constructors.length != 1) {
            throw new IllegalStateException("CacheImpl class should have only constructor");
        }
        return (Constructor<? extends T>) constructors[0];
    }

    private static final class WrapperMethodGenerator extends GeneratorAdapter {
        private WrapperMethodGenerator(int access, Method method, MethodVisitor mv) {
            super(Opcodes.ASM4, mv, access, method.getName(), method.getDescriptor());
        }

        public void loadStorage(Type wrapperType, Type storageType) {
            loadThis();
            getField(wrapperType, STORAGE_FIELD, storageType);
        }
    }

    private static Constructor<Cache> generateObjectObjectCacheWrapperConstructor(Signature signature) throws NoSuchMethodException {
        Constructor<Cache> result = CACHE_WRAPPER_CONSTRUCTORS_CACHE.get(signature);
        if (result != null) {
            return result;
        }

        Class<? extends Cache> cacheInterface = signature.getCacheInterface();
        ClassGenerator v = new ClassGenerator(Opcodes.ACC_PUBLIC, Type.getInternalName(Wrapping.class) + "$cache$" + WRAPPER_ID.getAndIncrement(), AbstractObjectObjectCacheWrapper.class, cacheInterface);

        Type objectCacheType = Type.getType(ObjectObjectCache.class);

        MxConstructorGenerator ctor = v.defineConstructor(Opcodes.ACC_PUBLIC, objectCacheType);
        ctor.start();
        ctor.callSuper(objectCacheType);
        ctor.returnValue();
        ctor.endMethod();

        MxGeneratorAdapter calculate;
        Type containerType = signature.getContainerType();
        if (containerType == null) {
            calculate = v.defineMethod(Opcodes.ACC_PUBLIC, "getOrCreate", Type.getType(signature.getValue()));
        } else {
            calculate = v.defineMethod(Opcodes.ACC_PUBLIC, "getOrCreate", Type.getType(signature.getValue()), containerType);
        }
        calculate.start();
        calculate.loadThis();
        calculate.getField(v.getThisType(), "delegate", objectCacheType);
        if (containerType == null) {
            calculate.pushNull();
        } else {
            calculate.loadArg(0);
            calculate.box(containerType);
        }
        calculate.invokeInterface(objectCacheType, new Method("getOrCreate", CodegenHelper.OBJECT_TYPE, new Type[]{CodegenHelper.OBJECT_TYPE}));
        calculate.unbox(Type.getType(signature.getValue()));
        calculate.returnValue();
        calculate.endMethod();

        v.visitEnd();
        //noinspection unchecked
        result = v.<Cache>toClass(Wrapping.class.getClassLoader()).getConstructor(ObjectObjectCache.class);
        CACHE_WRAPPER_CONSTRUCTORS_CACHE.put(signature, result);
        return result;
    }

    public static <K, V> Cache getObjectObjectCacheWrapper(ObjectObjectCache<K, V> cache) {
        Signature signature = cache.getDescriptor().getSignature().erased();
        if (signature.getContainer() == Object.class && signature.getValue() == Object.class) {
            return cache;
        }
        try {
            return generateObjectObjectCacheWrapperConstructor(signature).newInstance(cache);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            return rethrowInvocationTargetException(e);
        }
    }

    private static Constructor<ObjectObjectCalculatable> generateCalculatableWrapperConstructor(Signature signature) throws NoSuchMethodException {
        Constructor<ObjectObjectCalculatable> result = CALCULABLE_WRAPPER_CONSTRUCTORS_CACHE.get(signature);
        if (result != null) {
            return result;
        }

        ClassGenerator v = new ClassGenerator(Opcodes.ACC_PUBLIC, Type.getInternalName(Wrapping.class) + "$calculableWrapper$" + WRAPPER_ID.getAndIncrement(), Object.class, ObjectObjectCalculatable.class);

        Class<?> calculableInterface = signature.getCalculableInterface();
        Type calculableType = Type.getType(calculableInterface);

        MxField delegateField = v.defineField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "calculable", calculableType);

        MxConstructorGenerator ctor = v.defineConstructor(Opcodes.ACC_PUBLIC, calculableType);
        ctor.start();
        ctor.callSuper();
        ctor.initFields(delegateField);
        ctor.returnValue();
        ctor.endMethod();

        MxGeneratorAdapter calculate = v.defineMethod(Opcodes.ACC_PUBLIC, "calculate", CodegenHelper.OBJECT_TYPE, CodegenHelper.OBJECT_TYPE, CodegenHelper.OBJECT_TYPE);
        calculate.start();
        calculate.get(delegateField);
        calculate.loadArg(0);
        Type containerType = signature.getContainerType();
        if (containerType == null) {
            calculate.invokeInterface(calculableType, new Method("calculate", Type.getType(signature.getValue()), new Type[]{CodegenHelper.OBJECT_TYPE}));
        } else {
            calculate.loadArg(1);
            calculate.unbox(containerType);
            calculate.invokeInterface(calculableType, new Method("calculate", Type.getType(signature.getValue()), new Type[]{CodegenHelper.OBJECT_TYPE, containerType}));
        }
        calculate.box(Type.getType(signature.getValue()));
        calculate.returnValue();
        calculate.endMethod();

        v.visitEnd();

        //noinspection unchecked
        result = v.<ObjectObjectCalculatable>toClass(Wrapping.class.getClassLoader()).getConstructor(calculableInterface);
        CALCULABLE_WRAPPER_CONSTRUCTORS_CACHE.put(signature, result);
        return result;
    }

    public static ObjectObjectCalculatable getCalculableWrapper(Signature signature, Object calculable) {
        if (calculable instanceof ObjectObjectCalculatable) {
            //noinspection unchecked
            return (ObjectObjectCalculatable) calculable;
        }
        try {
            return Wrapping.generateCalculatableWrapperConstructor(signature).newInstance(calculable);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            return rethrowInvocationTargetException(e);
        }
    }

    private static <T> T rethrowInvocationTargetException(InvocationTargetException e) {
        if (e.getTargetException() instanceof Error) {
            throw (Error) e.getTargetException();
        }
        if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
        }
        throw new RuntimeException(e);
    }
}
