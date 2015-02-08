/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.storage.elementlocked.ElementLockedStorage;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class Signature {
    private static final String CACHES_PACKAGE_NAME = "com.maxifier.mxcache.caches.";
    private static final String STORAGE_PACKAGE_NAME = "com.maxifier.mxcache.storage.";
    private static final String LOCKED_STORAGE_PACKAGE_NAME = "com.maxifier.mxcache.storage.elementlocked.";
    private static final String SINGLETON_NODES_PACKAGE_NAME = "com.maxifier.mxcache.impl.resource.nodes.ViewableSingleton";
    private static final int PRIMITIVE_TYPE_COUNT = 8;

    private static final Class[] BASIC_TYPES = {boolean.class, byte.class, short.class, char.class,
            int.class, long.class, float.class, double.class, Object.class, null};

    private static final Map<Class, Map<Class, Signature>> BASIC;
    private static final Map<Class, Signature> CACHE;

    private static final Map<Class, String> PRIMITIVE_NAMES = createPrimitiveNamesCache();

    static {
        CACHE = new WeakHashMap<Class, Signature>(BASIC_TYPES.length * (BASIC_TYPES.length - 1));
        BASIC = new THashMap<Class, Map<Class, Signature>>(BASIC_TYPES.length);
        for (Class e : BASIC_TYPES) {
            Map<Class, Signature> byValue = new THashMap<Class, Signature>(BASIC_TYPES.length - 1);
            BASIC.put(e, byValue);
            for (Class f : BASIC_TYPES) {
                if (f != null) {
                    Signature s = new BasicSignature(e, f);
                    CACHE.put(s.getCacheInterface(), s);
                    CACHE.put(s.getCalculableInterface(), s);
                    CACHE.put(s.getStorageInterface(), s);
                    CACHE.put(s.getElementLockedStorageInterface(), s);
                    if (e == null) {
                        CACHE.put(s.getSingletonDependencyNodeInterface(), s);
                    }
                    byValue.put(f, s);
                }
            }
        }
    }

    private static final Signature UNKNOWN = new Signature(null, null);

    private static Map<Class, String> createPrimitiveNamesCache() {
        Map<Class, String> primitiveNames = new THashMap<Class, String>(PRIMITIVE_TYPE_COUNT);
        primitiveNames.put(boolean.class, "Boolean");
        primitiveNames.put(byte.class, "Byte");
        primitiveNames.put(short.class, "Short");
        primitiveNames.put(char.class, "Character");
        primitiveNames.put(int.class, "Int");
        primitiveNames.put(long.class, "Long");
        primitiveNames.put(float.class, "Float");
        primitiveNames.put(double.class, "Double");
        return primitiveNames;
    }

    private final Class[] keys;

    @Nullable
    private final Class container;

    private final Class value;

    private final Signature erased;

    public Signature erased() {
        return erased;
    }

    @Nonnull
    public static synchronized Signature of(@Nullable Class container, @Nonnull Class value) {
        Map<Class, Signature> byValue = BASIC.get(container);
        if (byValue == null) {
            return new Signature(container, value);
        }
        Signature signature = byValue.get(value);
        if (signature == null) {
            // don't save it
            return new Signature(container, value);
        }
        return signature;
    }

    @Nonnull
    public static synchronized Signature of(@Nonnull Class<?> c) {
        Signature s = CACHE.get(c);
        if (s == null) {
            s = extractSignature(c);
            CACHE.put(c, s);
        }
        if (s == UNKNOWN) {
            throw new IllegalArgumentException("No signature found for " + c);
        }
        return s;
    }

    @Nonnull
    private static synchronized Signature extractSignature(Class<?> c) {
        Signature res = null;
        Class p = c;
        do {
            for (Class intf : p.getInterfaces()) {
                // some cache classes implement storage interface
                // to avoid conflict we don't check storage types for cache.
                if (!Cache.class.isAssignableFrom(c) || !Storage.class.isAssignableFrom(intf)) {
                    Signature s = CACHE.get(intf);
                    if (s != null) {
                        if (res != null && !s.equals(res)) {
                            throw new IllegalArgumentException("Class " + c + " has too many signatures");
                        }
                        res = s;
                    }
                }
            }
            p = p.getSuperclass();
        } while (p != null);
        return res == null ? UNKNOWN : res;
    }

    public Signature(Class[] keys, Class tuple, Class value) {
        this.keys = keys;
        this.container = tuple;
        this.value = value;
        if (this instanceof BasicSignature) {
            erased = this;
        } else {
            erased = of(erase(tuple), erase(value));
        }
    }

    public Signature(Class key, Class value) {
        if (key == null) {
            this.keys = null;
            this.container = null;
        } else {
            this.keys = new Class[] {key};
            this.container = key;
        }
        this.value = value;

        if (value == null && key == null) {
            // for unknown: internal use only!
            if (UNKNOWN != null) {
                throw new IllegalArgumentException("value & key should not be null");
            }
            erased = null;
        } else if (this instanceof BasicSignature) {
            erased = this;
        } else {
            erased = of(erase(key), erase(value));
        }
    }

    private static Class erase(Class k) {
        if (k != null && !k.isPrimitive()) {
            return Object.class;
        }
        return k;
    }

    public boolean hasKeys() {
        return container != null;
    }

    @Nullable
    public Class getContainer() {
        return container;
    }

    public Type getContainerType() {
        return container == null ? null : Type.getType(container);
    }

    public Class getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Signature)) {
            return false;
        }

        Signature signature = (Signature) o;

        return container == signature.container && value == signature.value;

    }

    @Override
    public int hashCode() {
        if (container == null) {
            return value.hashCode();
        }
        return 31 * container.hashCode() + value.hashCode();
    }

    @Override
    public String toString() {
        if (container == null) {
            return value.getName() + "()";
        }
        StringBuilder b = new StringBuilder();
        b.append(value.getName()).append("(");
        if (keys == null) {
            b.append(container.getName());
        } else {
            b.append("Tuple:").append(Arrays.toString(keys));
        }
        b.append(")");
        return b.toString();
    }

    public Class<?> getImplementationClass(String prefix, String postfix) {
        return erased.getImplementationClass(prefix, postfix);
    }

    public String getImplementationClassName(String prefix, String postfix) {
        return erased.getImplementationClassName(prefix, postfix);
    }

    /**
     * This method transforms class into element of Cache class name:
     * primitives are capitalized, other classes are transformed to "Object".
     * E.g. 'int' becomes 'Int', 'String' becomes 'Object'.
     *
     * @param c class
     *
     * @return element of Cache class name
     */
    public static String toString(Class c) {
        if (c == null) {
            return "";
        }
        if (!c.isPrimitive()) {
            return "Object";
        }
        return PRIMITIVE_NAMES.get(c);
    }

    public Class getKey(int index) {
        return keys[index];
    }

    public int getKeyCount() {
        return keys.length;
    }

    public Class<? extends Cache> getCacheInterface() {
        return erased.getCacheInterface();
    }

    public Class<? extends Calculable> getCalculableInterface() {
        return erased.getCalculableInterface();
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends Storage> getStorageInterface() {
        return erased.getStorageInterface();
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends ElementLockedStorage> getElementLockedStorageInterface() {
        return erased.getElementLockedStorageInterface();
    }

    @SuppressWarnings({"unchecked"})
    public Class<? extends SingletonDependencyNode> getSingletonDependencyNodeInterface() {
        return erased.getSingletonDependencyNodeInterface();
    }

    /**
     * @param other other signature
     * @return true only if object with other signature can be transparently replaced with object with this signature,
     *         false if additional transformation is required.
     */
    public boolean isWider(Signature other) {
        return (container == other.container || (container != null && other.container != null && container.isAssignableFrom(other.container))) &&
                (value == other.value || value.isAssignableFrom(other.value));
    }

    public Signature overrideKey(Class key) {
        if (key == container && keys.length == 1 && keys[0] == key) {
            return this;
        }
        return of(key, value);
    }

    @PublicAPI
    public Signature overrideKeys(Class[] keys, Class container) {
        if (Arrays.equals(keys, this.keys) && container == this.container) {
            return this;
        }
        return new Signature(keys, container, value);
    }

    @PublicAPI
    public Signature overrideValue(Class value) {
        if (value == this.value) {
            return this;
        }
        return new Signature(keys, container, value);
    }

    @SuppressWarnings("unchecked")
    private static class BasicSignature extends Signature {
        private final Class<? extends Cache> cacheInterface;
        private final Class<? extends Storage> storageInterface;
        private final Class<? extends Calculable> calculatableInterface;
        private final Class<? extends ElementLockedStorage> elementLockedStorageInterface;
        private final Class<? extends SingletonDependencyNode> singletonDependencyNode;

        BasicSignature(Class key, Class value) {
            super(key, value);
            cacheInterface = (Class<? extends Cache>) getImplementationClass(CACHES_PACKAGE_NAME, "Cache");
            calculatableInterface = (Class<? extends Calculable>) getImplementationClass(CACHES_PACKAGE_NAME, "Calculatable");
            singletonDependencyNode = key == null ? (Class<? extends SingletonDependencyNode>) getImplementationClass(SINGLETON_NODES_PACKAGE_NAME, "DependencyNode") : null;

            // storage value is always boxed
            storageInterface = (Class<? extends Storage>) getImplementationClass(STORAGE_PACKAGE_NAME, key, Object.class, "Storage");
            elementLockedStorageInterface = (Class<? extends ElementLockedStorage>) getImplementationClass(LOCKED_STORAGE_PACKAGE_NAME, key, Object.class, "ElementLockedStorage");
        }

        @Override
        public Class<? extends Cache> getCacheInterface() {
            return cacheInterface;
        }

        @Override
        public Class<? extends Storage> getStorageInterface() {
            return storageInterface;
        }

        @Override
        public Class<? extends Calculable> getCalculableInterface() {
            return calculatableInterface;
        }

        @Override
        public Class<? extends ElementLockedStorage> getElementLockedStorageInterface() {
            return elementLockedStorageInterface;
        }

        @Override
        public Class<? extends SingletonDependencyNode> getSingletonDependencyNodeInterface() {
            return singletonDependencyNode;
        }

        public Class<?> getImplementationClass(String prefix, Class<?> container, Class<?> value, String postfix) {
            return findClass(prefix + toString(container) + toString(value) + postfix);
        }

        public String getImplementationClassName(String prefix, String postfix) {
            return prefix + toString(getContainer()) + toString(getValue()) + postfix;
        }

        public Class<?> getImplementationClass(String prefix, String postfix) {
            return getImplementationClass(prefix, getContainer(), getValue(), postfix);
        }

        private static Class<?> findClass(String implName) {
            try {
                return Class.forName(implName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("No implementation class found (suggested implementation: " + implName + ")");
            }
        }
    }
}
