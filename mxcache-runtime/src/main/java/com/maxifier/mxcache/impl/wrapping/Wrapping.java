package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.asm.ClassWriter;
import com.maxifier.mxcache.asm.Label;
import com.maxifier.mxcache.asm.MethodVisitor;
import com.maxifier.mxcache.asm.commons.GeneratorAdapter;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.caches.Cache;
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
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import static com.maxifier.mxcache.asm.Opcodes.*;
import static com.maxifier.mxcache.asm.Type.BOOLEAN_TYPE;
import static com.maxifier.mxcache.asm.Type.VOID_TYPE;
import static com.maxifier.mxcache.util.CodegenHelper.*;
import static com.maxifier.mxcache.transform.TransformGenerator.NO_TRANSFORM;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.09.2010
 * Time: 19:41:46
 */
public final class Wrapping {
    private static final Type MUTABLE_STATISTICS_TYPE = Type.getType(MutableStatistics.class);
    private static final Type STORAGE_TYPE = Type.getType(Storage.class);
    private static final Type STATISTICS_HOLDER_TYPE = Type.getType(StatisticsHolder.class);
    private static final Type STORAGE_HOLDER_TYPE = Type.getType(StorageHolder.class);
    private static final Type LOCK_TYPE = Type.getType(Lock.class);

    private static final String STORAGE_FIELD = "storage";

    private static final String LOAD_METHOD = "load";
    private static final String IS_CALCULATED_METHOD = "isCalculated";
    private static final String SAVE_METHOD = "save";
    private static final String LOCK_METHOD = "lock";
    private static final String UNLOCK_METHOD = "unlock";

    private static final String UNDEFINED_CONST_NAME = "UNDEFINED";

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


    public static WrapperFactory getFactory(Signature storageSignature, Signature cacheSignature, boolean perElementLocking) {
        return getFactory(storageSignature, cacheSignature, null, null, perElementLocking);
    }

    public static WrapperFactory getFactory(Signature storageSignature, Signature cacheSignature, @Nullable TransformGenerator userKeyTransformer, @Nullable TransformGenerator userValueTransformer, boolean perElementLocking) {
        if (isNoTransform(userKeyTransformer) && isNoTransform(userValueTransformer) && storageSignature.isWider(cacheSignature)) {
            return getPlainFactory(storageSignature, perElementLocking);
        }
        return getConvertingFactory(storageSignature, cacheSignature, wrapNullTransform(userKeyTransformer), wrapNullTransform(userValueTransformer), perElementLocking);
    }

    private static boolean isNoTransform(TransformGenerator userKeyTransformer) {
        return (userKeyTransformer == null || userKeyTransformer == NO_TRANSFORM);
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

    private static synchronized WrapperFactory getConvertingFactory(Signature storageSignature, Signature cacheSignature, @NotNull TransformGenerator userKeyTransformer, @NotNull TransformGenerator userValueTransformer, boolean perElementLocking) {
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

    private static TransformGenerator wrapNullTransform(TransformGenerator transform) {
        return transform == null ? NO_TRANSFORM : transform;
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

    private static WrapperFactory createConvertingFactory(Signature storageSignature, Signature cacheSignature, CacheImplementationSignature signature, @NotNull TransformGenerator userKeyTransformer, @NotNull TransformGenerator userValueTransformer, boolean perElementLocking) {
        @NotNull
        TransformGenerator keyTransformer = boxTransformer(userKeyTransformer, cacheSignature.getContainer(), storageSignature.getContainer());
        @NotNull
        TransformGenerator valueTransformer = boxTransformer(userValueTransformer, cacheSignature.getValue(), storageSignature.getValue());

        byte[] bytecode = generateWrapperBytecode(storageSignature, cacheSignature, keyTransformer, valueTransformer, signature, perElementLocking);

        Class<? extends Cache> wrapperClass = loadClass(Wrapping.class.getClassLoader(), bytecode);

        return new WrapperFactoryImpl(getOnlyConstructor(wrapperClass));
    }

    @NotNull
    private static TransformGenerator boxTransformer(TransformGenerator transformer, Class from, Class to) {
        return ChainedTransformGenerator.chain(transformer, getBoxingTransformGenerator(transformer.getTransformedType(from), to));
    }

    private static byte[] generateWrapperBytecode(Signature storageSignature, Signature cacheSignature, @NotNull TransformGenerator keyTransformer, @NotNull TransformGenerator valueTransformer, CacheImplementationSignature signature, boolean perElementLocking) {
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
        Type erasedCacheValueType = erase(Type.getType(cacheSignature.getValue()));

        Type storageKeyType = storageSignature.getContainerType();
        Type storageValueType = Type.getType(storageSignature.getValue());

        if (needsIsCalculated(erasedCacheValueType)) {
            generateIsCalculated(keyTransformer, w, wrapperType, storageType, erasedCacheKeyType, storageKeyType, storageValueType);
        }

        generateLoad(keyTransformer, valueTransformer, w, wrapperType, storageType, erasedCacheKeyType, erasedCacheValueType, storageKeyType, storageValueType);

        generateSave(keyTransformer, valueTransformer, w, wrapperType, storageType, erasedCacheKeyType, erasedCacheValueType, storageKeyType, storageValueType);

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

    private static void generateSetStorage(ClassWriter w, Type wrapperType, Type storageType) {
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

    private static void generateGetStatistics(ClassWriter w, String superName, Type wrapperType, Type storageType) {
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
        getStatistics.invokeSuper(Type.getObjectType(superName), GET_STATISTICS_METHOD);
        getStatistics.returnValue();
        getStatistics.endMethod();
    }

    private static void generateSave(@NotNull TransformGenerator keyTransformer, @NotNull TransformGenerator valueTransformer, ClassWriter w, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type erasedCacheValueType, Type storageKeyType, Type storageValueType) {
        Method cacheSaveMethod = new Method(SAVE_METHOD, VOID_TYPE, erasedCacheKeyType == null ? new Type[]{erasedCacheValueType} : new Type[]{erasedCacheKeyType, erasedCacheValueType});
        Method storageSaveMethod = new Method(SAVE_METHOD, VOID_TYPE, storageKeyType == null ? new Type[]{erase(storageValueType)} : new Type[]{erase(storageKeyType), erase(storageValueType)});
        WrapperMethodGenerator save = defineMethod(w, cacheSaveMethod);
        save.visitCode();

        save.loadStorage(wrapperType, storageType);

        if (erasedCacheKeyType != null) {
            generateArgumentForwardTransform(keyTransformer, wrapperType, save);
        }
        save.loadArg(erasedCacheKeyType == null ? 0 : 1);
        valueTransformer.generateForward(wrapperType, keyTransformer.getFieldCount(), save);
        save.invokeInterface(storageType, storageSaveMethod);
        save.returnValue();

        save.endMethod();
    }

    private static void generateDelegatingLockingMethod(String name, @NotNull TransformGenerator keyTransformer, ClassWriter w, Type wrapperType, Type storageType, @NotNull Type erasedCacheKeyType, @NotNull Type storageKeyType) {
        Method cacheLockMethod = new Method(name, VOID_TYPE, new Type[]{erasedCacheKeyType});
        Method storageLockMethod = new Method(name, VOID_TYPE, new Type[]{erase(storageKeyType)});
        WrapperMethodGenerator save = defineMethod(w, cacheLockMethod);

        save.visitCode();
        save.loadStorage(wrapperType, storageType);

        generateArgumentForwardTransform(keyTransformer, wrapperType, save);
        save.invokeInterface(storageType, storageLockMethod);
        save.returnValue();

        save.endMethod();
    }

    private static void generateGetLock(ClassWriter w, Type wrapperType, Type storageType) {
        WrapperMethodGenerator save = defineMethod(w, GET_LOCK_METHOD);

        save.visitCode();
        save.loadStorage(wrapperType, storageType);
        save.invokeInterface(storageType, GET_LOCK_METHOD);
        save.returnValue();

        save.endMethod();
    }

    private static void generateDelegatingMethod(ClassWriter w, Type wrapperType, Type storageType, Method method) {
        WrapperMethodGenerator size = defineMethod(w, method);
        size.loadStorage(wrapperType, storageType);
        size.invokeInterface(storageType, method);
        size.returnValue();
        size.endMethod();
    }

    private static Type[] getArgumentTypes(@Nullable Type type) {
        return type == null ? EMPTY_TYPES : new Type[]{erase(type)};
    }

    private static void generateIsCalculated(@NotNull TransformGenerator keyTransformer, ClassWriter w, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type storageKeyType, Type storageValueType) {
        Method cacheIsCalculatedMethod = new Method(IS_CALCULATED_METHOD, BOOLEAN_TYPE, getArgumentTypes(erasedCacheKeyType));

        WrapperMethodGenerator isCalculated = defineMethod(w, cacheIsCalculatedMethod);

        isCalculated.loadStorage(wrapperType, storageType);
        if (needsIsCalculated(storageValueType)) {
            generateDelegatingIsCalculated(keyTransformer, wrapperType, storageType, erasedCacheKeyType, storageKeyType, isCalculated);
        } else {
            generateIsCalculatedViaLoad(keyTransformer, wrapperType, storageType, erasedCacheKeyType, storageKeyType, storageValueType, isCalculated);
        }
        isCalculated.endMethod();
    }

    private static void generateDelegatingIsCalculated(TransformGenerator keyTransformer, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type storageKeyType, WrapperMethodGenerator isCalculated) {
        Method storageIsCalculatedMethod = new Method(IS_CALCULATED_METHOD, BOOLEAN_TYPE, getArgumentTypes(storageKeyType));

        if (erasedCacheKeyType != null) {
            generateArgumentForwardTransform(keyTransformer, wrapperType, isCalculated);
        }

        isCalculated.invokeInterface(storageType, storageIsCalculatedMethod);
        isCalculated.returnValue();
    }

    private static void generateIsCalculatedViaLoad(TransformGenerator keyTransformer, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type storageKeyType, Type storageValueType, WrapperMethodGenerator isCalculated) {
        Method storageLoadMethod = new Method(LOAD_METHOD, erase(storageValueType), getArgumentTypes(storageKeyType));

        if (erasedCacheKeyType != null) {
            generateArgumentForwardTransform(keyTransformer, wrapperType, isCalculated);
        }

        isCalculated.invokeInterface(storageType, storageLoadMethod);
        isCalculated.loadUndefined();
        Label calculated = new Label();
        isCalculated.ifCmp(OBJECT_TYPE, GeneratorAdapter.NE, calculated);

        isCalculated.push(false);
        isCalculated.returnValue();

        isCalculated.mark(calculated);
        isCalculated.push(true);
        isCalculated.returnValue();
    }

    private static void generateArgumentForwardTransform(TransformGenerator keyTransformer, Type wrapperType, WrapperMethodGenerator methodGenerator) {
        methodGenerator.loadArg(0);
        keyTransformer.generateForward(wrapperType, 0, methodGenerator);
    }

    private static void generateLoad(@NotNull TransformGenerator keyTransformer, @NotNull TransformGenerator valueTransformer, ClassWriter w, Type wrapperType, Type storageType, Type erasedCacheKeyType, Type erasedCacheValueType, Type storageKeyType, Type storageValueType) {
        Method cacheLoadMethod = new Method(LOAD_METHOD, erasedCacheValueType, getArgumentTypes(erasedCacheKeyType));
        Method storageLoadMethod = new Method(LOAD_METHOD, erase(storageValueType), getArgumentTypes(storageKeyType));

        WrapperMethodGenerator load = defineMethod(w, cacheLoadMethod);
        load.loadStorage(wrapperType, storageType);
        if (needsIsCalculated(storageValueType) && !needsIsCalculated(erasedCacheValueType)) {
            int transformedKey = -1;

            if (erasedCacheKeyType != null) {
                generateArgumentForwardTransform(keyTransformer, wrapperType, load);
                transformedKey = load.newLocal(storageKeyType);
                load.dup();
                load.storeLocal(transformedKey);
            }

            Method storageIsCalculatedMethod = new Method(IS_CALCULATED_METHOD, BOOLEAN_TYPE, getArgumentTypes(storageKeyType));
            load.invokeInterface(storageType, storageIsCalculatedMethod);

            Label calculated = new Label();

            load.ifZCmp(GeneratorAdapter.NE, calculated);

            load.loadUndefined();
            load.returnValue();

            load.mark(calculated);
            load.loadStorage(wrapperType, storageType);

            if (erasedCacheKeyType != null) {
                load.loadLocal(transformedKey);
            }
        } else if (erasedCacheKeyType != null) {
            generateArgumentForwardTransform(keyTransformer, wrapperType, load);
        }
        load.invokeInterface(storageType, storageLoadMethod);
        valueTransformer.generateBackward(wrapperType, keyTransformer.getFieldCount(), load);
        load.returnValue();

        load.endMethod();
    }

    private static boolean needsIsCalculated(Type storageValueType) {
        return !isReferenceType(storageValueType);
    }

    private static void generateConstructor(ClassWriter w, Type superType, Type calculatableType) {
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

    private static WrapperMethodGenerator defineMethod(ClassWriter w, Method ctorMethod) {
        return new WrapperMethodGenerator(ACC_PUBLIC, ctorMethod, w.visitMethod(ACC_PUBLIC, ctorMethod.getName(), ctorMethod.getDescriptor(), null, null));
    }

    private static String getCacheImplClassName(Signature storageSignature, Signature cacheSignature) {
        String extension = storageSignature.getImplementationClassName("$", "CacheImpl$" + WRAPPER_ID.getAndIncrement());
        return cacheSignature.getImplementationClassName("com/maxifier/mxcache/impl/caches/wrapping/Storage", extension);
    }

    @NotNull
    private static TransformGenerator getBoxingTransformGenerator(Class from, Class to) {
        if (from == to) {
            return NO_TRANSFORM;
        }
        if (from != null && to != null) {
            if (to.isAssignableFrom(from)) {
                return NO_TRANSFORM;
            }
            if (to.isPrimitive() && !from.isPrimitive() && from.isAssignableFrom(getBoxedType(to))) {
                return new UnboxTransformGenerator(to);
            }
            if (from.isPrimitive() && !to.isPrimitive() && to.isAssignableFrom(getBoxedType(from))) {
                return new BoxTransformGenerator(Type.getType(from));
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
            super(access, method, mv);
        }

        public void loadUndefined() {
            getStatic(STORAGE_TYPE, UNDEFINED_CONST_NAME, OBJECT_TYPE);
        }

        public void loadStorage(Type wrapperType, Type storageType) {
            loadThis();
            getField(wrapperType, STORAGE_FIELD, storageType);
        }
    }
}
