package com.maxifier.mxcache.util;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.asm.util.CheckClassAdapter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import com.maxifier.mxcache.asm.Type;
import org.apache.commons.io.IOUtils;

import static com.maxifier.mxcache.asm.Opcodes.ACC_PRIVATE;
import static com.maxifier.mxcache.asm.Opcodes.ACC_STATIC;
import static com.maxifier.mxcache.asm.Opcodes.ACC_SYNTHETIC;
import static com.maxifier.mxcache.asm.Type.ARRAY;
import static com.maxifier.mxcache.asm.Type.OBJECT;
import static com.maxifier.mxcache.asm.Type.getType;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.02.2010
 * Time: 19:15:27
 */
public final class CodegenHelper {
    public static final Type STRING_TYPE = getType(String.class);
    public static final Type OBJECT_TYPE = getType(Object.class);
    public static final Type CLASS_TYPE = getType(Class.class);

    private static final String CLASS_DUMP_PATH = "C:/dump/";//System.getProperty("mxcache.dump.path");

    private static final java.lang.reflect.Method DEFINE_CLASS_METHOD = getDefineClassMethod();

    public static final Type[] EMPTY_TYPES = { };

    public static final String CONSTRUCTOR_NAME = "<init>";
    public static final String STATIC_INITIALIZER_NAME = "<clinit>";

    public static final Method NO_ARG_CONSTRUCTOR = Method.getMethod("void <init>()");

    public static final int STATIC_INITIALIZER_ACCESS = ACC_PRIVATE | ACC_SYNTHETIC | ACC_STATIC;

    private static final Class[] CLASS_BY_SORT = createClassBySortArray();

    private static final Map<Class, Class> BOXING_TYPES = new THashMapBuilder<Class, Class>()
            .put(boolean.class, Boolean.class)
            .put(byte.class, Byte.class)
            .put(short.class, Short.class)
            .put(char.class, Character.class)
            .put(int.class, Integer.class)
            .put(long.class, Long.class)
            .put(float.class, Float.class)
            .put(double.class, Double.class)
            .toMap();

    private static java.lang.reflect.Method getDefineClassMethod() {
        return AccessController.doPrivileged(new PrivilegedAction<java.lang.reflect.Method>() {
            @Override
            public java.lang.reflect.Method run() {
                try {
                    java.lang.reflect.Method res = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                    res.setAccessible(true);
                    return res;
                } catch (Exception e) {
                    throw new IllegalStateException("Cannot find define class method", e);
                }
            }
        });
    }

    private static Class[] createClassBySortArray() {
        Class[] res = new Class[Type.DOUBLE + 1];
        res[Type.VOID] = void.class;
        res[Type.BOOLEAN] = boolean.class;
        res[Type.CHAR] = char.class;
        res[Type.BYTE] = byte.class;
        res[Type.SHORT] = short.class;
        res[Type.CHAR] = char.class;
        res[Type.INT] = int.class;
        res[Type.FLOAT] = float.class;
        res[Type.LONG] = long.class;
        res[Type.DOUBLE] = double.class;
        return res;
    }

    private CodegenHelper() {
    }

    /**
     * Use this method only for debug!!!
     * @param bytecode bytecode of class to verify
     */
    @Deprecated
    public static void verify(byte[] bytecode) {
        CheckClassAdapter.verify(new ClassReader(bytecode), true, new PrintWriter(System.out));
    }

    /**
     * Use this method only for debug!!!
     *
     * @param bytecode bytecode of class to verify
     * @param loader main class loader
     */
    @Deprecated
    public static void verify(byte[] bytecode, ClassLoader loader) {
        CheckClassAdapter.verify(new ClassReader(bytecode), loader, true, new PrintWriter(System.out));
    }

    /**
     * Use this method only for debug!!!
     * @param bytecode bytecode of class to dump
     */
    @Deprecated
    public static void dumpClass(byte[] bytecode) {
        String className = getClassName(bytecode);
        dumpClass(className, bytecode);
    }

    private static void dumpClass(String className, byte[] bytecode) {
        try {
            String fileName = className.replace('/', '.') + ".class";
            FileOutputStream fos = new FileOutputStream(new File(CLASS_DUMP_PATH, fileName));
            try {
                fos.write(bytecode);
            } finally {
                fos.close();
            }
        } catch (IOException e) {
            // ignore - dumping is not important
        }
    }

    public static String getClassName(byte[] bytecode) {
        ClassReader r = new ClassReader(bytecode);
        NameFindVisitor v = new NameFindVisitor();
        r.accept(v, 0);
        return v.getName();
    }

    public static <T> Class<T> loadClass(ClassLoader classLoader, byte[] bytecode) {
        try {
            //noinspection unchecked
            return (Class<T>) DEFINE_CLASS_METHOD.invoke(classLoader, null, bytecode, 0, bytecode.length);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static java.lang.reflect.Method getMethod(Class owner, String name, String desc) {
        try {
            Type[] types = Type.getArgumentTypes(desc);
            Class[] args = toClass(types);
            return owner.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static Class[] toClass(Type[] types) {
        Class[] args = new Class[types.length];
        for (int i = 0; i<types.length; i++) {
            args[i] = toClass(types[i]);
        }
        return args;
    }

    public static Class<?> toClass(Type type) {
        int sort = type.getSort();
        if (sort == Type.ARRAY) {
            try {
                return Class.forName(getBinaryName(type));
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (sort == Type.OBJECT) {
            try {
                return Class.forName(type.getClassName());
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException(e);
            }
        }
        if (sort >= 0 && sort < CLASS_BY_SORT.length) {
            Class res = CLASS_BY_SORT[sort];
            if (res != null) {
                return res;
            }
        }
        throw new UnsupportedOperationException("Unknown type: " + type);
    }

    private static String getBinaryName(Type type) {
        return type.getDescriptor().replace('/', '.');
    }

    public static byte[] getByteCode(Class c) throws IOException {
        InputStream stream = c.getClassLoader().getResourceAsStream(Type.getType(c).getInternalName() + ".class");
        try {
            return IOUtils.toByteArray(stream);
        } finally {
            stream.close();
        }
    }

    public static Class erase(Class type) {
        return type.isPrimitive() ? type : Object.class;
    }

    public static Class[] erase(Class[] types) {
        Class[] res = new Class[types.length];
        for (int i = 0, typesLength = types.length; i < typesLength; i++) {
            res[i] = erase(types[i]);
        }
        return res;
    }

    public static Type erase(Type value) {
        if (value == null) {
            return null;
        }
        return isReferenceType(value) ? OBJECT_TYPE : value;
    }

    public static Type[] getTypes(Class[] classes) {
        Type[] types = new Type[classes.length];
        for (int i = 0; i<classes.length; i++) {
            types[i] = Type.getType(classes[i]);
        }
        return types;
    }

    public static Class[] getClasses(Type[] types) {
        Class[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            classes[i] = toClass(types[i]);
        }
        return classes;
    }

    public static Type[] erase(Type[] values) {
        Type[] types = new Type[values.length];
        for (int i = 0; i < values.length; i++) {
            types[i] = erase(values[i]);
        }
        return types;
    }

    public static Type[] toErasedTypes(Class[] values) {
        Type[] types = new Type[values.length];
        for (int i = 0; i < values.length; i++) {
            types[i] = erase(getType(values[i]));
        }
        return types;
    }

    public static boolean isReferenceType(Type cacheKeyType) {
        int sort = cacheKeyType.getSort();
        return sort == ARRAY || sort == OBJECT;
    }

    public static Type[] insertFirst(Type[] arguments, Type first) {
        Type[] ctorArguments = new Type[arguments.length + 1];
        ctorArguments[0] = first;
        System.arraycopy(arguments, 0, ctorArguments, 1, arguments.length);
        return ctorArguments;
    }

    /**
     * @param t type to examine
     * @return corresponding wrapper type for given type <code>t</code>, or null if <code>t</code> is not primitive
     */
    public static Class getBoxedType(Class t) {
        return BOXING_TYPES.get(t);
    }
}
