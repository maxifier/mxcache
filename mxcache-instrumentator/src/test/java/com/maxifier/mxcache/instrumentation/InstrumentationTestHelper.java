package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import com.maxifier.mxcache.util.CodegenHelper;
import org.apache.commons.io.input.ClassLoaderObjectInputStream;
import org.testng.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 11:42:34
 */
public final class InstrumentationTestHelper {
    private InstrumentationTestHelper() {
    }

    public static Class<?> instrumentClass(Class<?> srcClass) throws IOException, ClassNotFoundException {
        return instrumentClass(srcClass, InstrumentatorImpl.LAST_VERSION, null);
    }

    public static Class<?> instrumentClass(Class<?> srcClass, Instrumentator instrumentator, ClassLoader cl) throws IOException, ClassNotFoundException {
        cl = new ClassLoader(cl == null ? ClassLoader.getSystemClassLoader() : cl) {};
        byte[] bytes = CodegenHelper.getByteCode(srcClass);
        return instrumentAndLoad(instrumentator, cl, bytes);
    }

    public static Class instrumentAndLoad(Instrumentator instrumentator, ClassLoader cl, byte[] bytes) {
        ClassInstrumentationResult res = instrumentator.instrument(bytes);
        for (ClassDefinition classDefinition : res.getAdditionalClasses()) {
            if (instrumentator.getVersion().equals("2.2.9")) {
                Assert.assertNull(instrumentator.instrument(classDefinition.getBytecode()));
            }
            CodegenHelper.loadClass(cl, classDefinition.getBytecode());
        }
        if (instrumentator.getVersion().equals("2.2.9")) {
            Assert.assertNull(instrumentator.instrument(res.getInstrumentedBytecode()));
        }
        return CodegenHelper.loadClass(cl, res.getInstrumentedBytecode());
//        CodegenHelper.dumpClass(res.getInstrumentedBytecode());
    }

    @SuppressWarnings({ "unchecked" })
    public static <T> T readWrite(T t) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(t);
        } finally {
            oos.close();
        }
        return (T)new ClassLoaderObjectInputStream(t.getClass().getClassLoader(), new ByteArrayInputStream(bos.toByteArray())).readObject();
    }
}
