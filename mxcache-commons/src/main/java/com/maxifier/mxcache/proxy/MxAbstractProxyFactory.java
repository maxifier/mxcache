package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.util.CodegenHelper;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.Type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.12.2009
 * Time: 13:02:32
 */
public class MxAbstractProxyFactory {
    protected static final Type[] EMPTY_TYPES = {};
    protected static final String GETTER_NAME = "getValue";
    protected static final Type RESOLVABLE_TYPE = Type.getType(Resolvable.class);

    static {
        enshureCompatibleMxContainer();
    }

    protected static void enshureCompatibleMxContainer() {
        try {
            if (!Resolvable.class.getMethod(GETTER_NAME).getReturnType().equals(Object.class)) {
                throw new IllegalStateException("MxContainer." + GETTER_NAME + " should have Object return type");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(Resolvable.class + " should have method getValue()");
        }
    }

    private static byte[] getClassBytes(ClassGen proxyClass) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        proxyClass.getJavaClass().dump(baos);
        return baos.toByteArray();
    }

    protected static <T> Class<T> loadClass(Class<T> sourceClass, ClassGen proxyClass) throws IOException, ClassNotFoundException {
        byte[] classData = getClassBytes(proxyClass);
        return CodegenHelper.loadClass(sourceClass.getClassLoader(), classData);
    }

    protected static String createProxyClassName(Class sourceClass) {
        return sourceClass.getName() + "$MxProxy";
    }
}
