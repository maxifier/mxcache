package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.caches.abs.elementlocked.ElementLockedStorage;
import com.maxifier.mxcache.storage.Storage;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.09.2010
 * Time: 19:40:24
 */
public final class Signature {
    private static final String CACHES_PACKAGE_NAME = "com.maxifier.mxcache.caches.";
    private static final String STORAGE_PACKAGE_NAME = "com.maxifier.mxcache.storage.";
    private static final String LOCKED_STORAGE_PACKAGE_NAME = "com.maxifier.mxcache.storage.elementlocked.";

    private final Class[] keys;
    
    private final Class container;

    private final Class value;

    private static final Class[] BASIC_TYPES = { boolean.class, byte.class, short.class, char.class,
            int.class, long.class, float.class, double.class, Object.class, null };

    private static final Map<Class, Signature> CACHE = createCache();

    private static final Signature UNKNOWN = new Signature(null, null);

    private static Map<Class, Signature> createCache() {
        Map<Class, Signature> res = new WeakHashMap<Class, Signature>();
        for (Class e : BASIC_TYPES) {
            for (Class f : BASIC_TYPES) {
                if (f != null) {
                    Signature s = new Signature(e, f);
                    res.put(s.getCacheInterface(), s);
                    res.put(s.getCalculableInterface(), s);
                    res.put(s.getStorageInterface(), s);
                    res.put(s.getElementLockedStorageInterface(), s);
                }
            }
        }
        return res;
    }

    public Signature erased() {
        Class erasedKey = container == null ? null : erase(container);
        Class erasedValue = erase(value);
        if (erasedValue == value && erasedKey == container) {
            return this;
        }
        return new Signature(erasedKey, erasedValue);
    }

    private Class erase(Class k) {
        Class erasedKey;
        if (!k.isPrimitive() && k != Object.class) {
            erasedKey = Object.class;
        } else {
            erasedKey = k;
        }
        return erasedKey;
    }

    @NotNull
    public static synchronized Signature of(Class c) {
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

    @NotNull
    private static synchronized Signature extractSignature(Class c) {
        Signature res = null;
        Class p = c;
        do {
            for (Class intf : p.getInterfaces()) {
                if (intf.getName().startsWith(STORAGE_PACKAGE_NAME) && intf.getName().endsWith("Storage")) {
                    Signature s = CACHE.get(intf);
                    if (s != null) {
                        if (res != null && s.equals(res)) {
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

    public Signature(@NotNull Class[] keys, @NotNull Class tuple, Class value) {
        this.keys = keys;
        this.container = tuple;
        this.value = value;
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
    }

    public boolean hasKeys() {
        return container != null;
    }

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
        if (o == null || getClass() != o.getClass()) {
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
        } else {
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
    }

    public Class<?> getImplementationClass(String prefix, String postfix) {
        return findClass(getImplementationClassName(prefix, postfix));
    }

    private static Class<?> findClass(String implName) {
        try {
            return Class.forName(implName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("No implementation class found (suggested implementation: " + implName + ")");
        }
    }

    public String getImplementationClassName(String prefix, String postfix) {
        if (container == null) {
            return prefix + toString(value) + postfix;
        }
        return prefix + toString(container) + toString(value) + postfix;
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
        if (c == char.class) {
            return "Character";
        }                         
        if (c.isPrimitive()) {
            String n = c.getSimpleName();
            return Character.toUpperCase(n.charAt(0)) + n.substring(1);
        }
        return "Object";
    }
    
    public Class getKey(int index) {
        return keys[index];
    }
    
    public int getKeyCount() {
        return keys.length;
    }

    @SuppressWarnings({ "unchecked" })
    public Class<? extends Cache> getCacheInterface() {
        return (Class<? extends Cache>) getImplementationClass(CACHES_PACKAGE_NAME, "Cache");
    }

    public Class<?> getCalculableInterface() {
        return getImplementationClass(CACHES_PACKAGE_NAME, "Calculatable");
    }

    @SuppressWarnings({ "unchecked" })
    public Class<? extends Storage> getStorageInterface() {
        return (Class<? extends Storage>) getImplementationClass(STORAGE_PACKAGE_NAME, "Storage");
    }

    @SuppressWarnings({ "unchecked" })
    public Class<? extends ElementLockedStorage> getElementLockedStorageInterface() {
        return (Class<? extends ElementLockedStorage>) getImplementationClass(LOCKED_STORAGE_PACKAGE_NAME, "ElementLockedStorage");
    }

    /**
     * @param other other signature
     * @return true only if object with other signature can be transparently replaced with object with this signature,
     * false if additional transformation is required.
     */
    public boolean isWider(Signature other) {
        return (container == other.container || container.isAssignableFrom(other.container)) &&
                (value == other.value || value.isAssignableFrom(other.value));
    }

    public Signature overrideKey(Class key) {
        if (key == container && keys.length == 1 && keys[0] == key) {
            return this;
        }
        return new Signature(key, value);
    }
    
    public Signature overrideKeys(Class[] keys, Class container) {
        if (Arrays.equals(keys, this.keys) && container == this.container) {
            return this;
        }
        return new Signature(keys, container, value);
    }

    public Signature overrideValue(Class value) {
        if (value == this.value) {
            return this;
        }
        return new Signature(keys, container, value);
    }
}
