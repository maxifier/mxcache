/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import com.maxifier.mxcache.stubgen.exam.*;
import com.maxifier.mxcache.stubgen.lib.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.WriterOutputStream;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * TestGenerator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-07 11:30)
 */
public class TestStubGen {
    private static final Class[] LIB_CLASSES = {
            SomeInterface.class,
            Subclass.class,
            Subclass.class.getSuperclass(),
            ExtendedEnum.class,
            InterfaceA.class,
            ClassX.class,
            InterfaceZ.class,
            UnusedClass.class,
            OuterClass.NestedClass.class,
            OuterClass.InnerClass.class,
            OuterClass.UnusedInnerClass.class,
            OuterClass.class,
            MyEnum.class,
            ClassWithFields.class,
            RecursiveGeneric.class,
            TestInputStream.class,
            ParentInterface.class,
            OtherInterface.class,
            ClassWithEntries.class,
            Subclass.EmptyIterator.class
    };

    private static final Class[] TEST_CLASSES = {
            TestA.class,
            TestNested.class,
            TestEnum.class,
            TestFields.class,
            TestInheritance.class,
            TestNoDefaultCtor.class,
            TestImportInnerClass.class
    };

    private File tempDir;
    private File libJar;
    private File examJar;
    private URLClassLoader stubClassLoader;

    @BeforeTest
    public void recompile() throws IOException, NoSuchMethodException, NoSuchFieldException, ClassNotFoundException, InvocationTargetException, IllegalAccessException {
        libJar = createJar(LIB_CLASSES);
        examJar = createJar(TEST_CLASSES);

        tempDir = new File(FileUtils.getTempDirectory(), File.createTempFile("stubgen", "").getName() + "dir");
        tempDir.mkdirs();

        URLClassLoader nonTestCl = getNonTestCL();

        Class<?> stubGenClass = nonTestCl.loadClass(StubGen.class.getName());
        Method mainMethod = stubGenClass.getMethod("main", String[].class);
        Object args = new String[]{"-e", examJar.getAbsolutePath(), "-l", libJar.getAbsolutePath(), "-o", tempDir.getAbsolutePath()};
        mainMethod.invoke(null, args);

        System.out.println(libJar);
        System.out.println(examJar);
        System.out.println(tempDir);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        Collection<File> allJavaFiles = FileUtils.listFiles(tempDir, new String[]{"java"}, true);

        if (allJavaFiles.isEmpty()) {
            fail("StubGen output is empty");
        }

        String[] allJavaPaths = new String[allJavaFiles.size()];
        int i = 0;
        for (File file : allJavaFiles) {
            allJavaPaths[i++] = file.getAbsolutePath();
        }

        StringWriter errorWriter = new StringWriter();

        compiler.run(null, null, new WriterOutputStream(errorWriter), allJavaPaths);

        String errors = errorWriter.toString();
        if (!errors.isEmpty()) {
            fail(errors);
        }

        stubClassLoader = new URLClassLoader(new URL[]{new URL("file:" + tempDir.getAbsolutePath() + "/")}, getClass().getClassLoader().getParent());
    }

    @AfterTest
    public void cleanup() throws IOException {
        FileUtils.deleteQuietly(libJar);
        FileUtils.deleteQuietly(examJar);
        FileUtils.deleteQuietly(tempDir);
    }

    @Test
    public void testNoDefaultCtor() throws ClassNotFoundException, NoSuchMethodException {
        getStubClass(TestInputStream.class).getDeclaredConstructor();
    }

    @Test
    public void testInheritance() throws ClassNotFoundException {
        Class<?> someInterface = getStubClass(SomeInterface.class);
        Runnable.class.isAssignableFrom(getStubClass(Subclass.class));
        someInterface.isAssignableFrom(getStubClass(Subclass.class));
        someInterface.isAssignableFrom(getStubClass(Subclass.class).getSuperclass());
    }

    @Test
    public void testRecursiveGeneric() throws ClassNotFoundException, NoSuchMethodException {
        getStubClass(RecursiveGeneric.class).getDeclaredMethod("complexMethod");
        getStubClass(RecursiveGeneric.class).getDeclaredMethod("get");
    }

    @Test
    public void testExtendedEnum() throws ClassNotFoundException, NoSuchMethodException {
        getStubClass(ExtendedEnum.class).getMethod("z");
    }

    @Test
    public void testMethodSignatures() {
        for (Class libClass : LIB_CLASSES) {
            try {
                Class<?> stub = getStubClass(libClass);
                for (Method srcMethod : libClass.getDeclaredMethods()) {
                    if (!srcMethod.isBridge()) {
                        Class<?>[] srcParameters = srcMethod.getParameterTypes();
                        Class[] stubParams = new Class[srcParameters.length];
                        for (int i = 0; i<srcParameters.length; i++) {
                            try {
                                stubParams[i] = getStubClass(srcParameters[i]);
                            } catch (ClassNotFoundException e) {
                                stubParams[i] = srcParameters[i];
                            }
                        }
                        try {
                            Method stubMethod = stub.getDeclaredMethod(srcMethod.getName(), stubParams);
                            assertEquals(stubMethod.toGenericString(), srcMethod.toGenericString());
                        } catch (NoSuchMethodException ignore) {
                        }
                    }
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
    }

    @Test
    public void testEmptyIterator() throws ClassNotFoundException, NoSuchMethodException {
        Class<?> emptyIteratorType = getStubClass(Subclass.EmptyIterator.class);
        Method iterator = getStubClass(Subclass.class).getMethod("iterator");
        assertEquals(iterator.getReturnType(), emptyIteratorType);
    }

    @Test
    public void testFieldInitializer() throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        URLClassLoader cl = new URLClassLoader(new URL[]{new URL("file:" + examJar.getAbsolutePath())}, stubClassLoader);
        Class<?> c = cl.loadClass(TestFields.class.getName());
        Method m = c.getMethod("testIt");
        assertEquals(m.invoke(c.newInstance()), "0 false null");
    }

    @Test(expectedExceptions = ClassNotFoundException.class)
    public void testUnusedClassNotCompiled() throws ClassNotFoundException{
        getStubClass(UnusedClass.class);
    }

    @Test(expectedExceptions = NoSuchMethodException.class)
    public void testUnusedMethodNotCompiled() throws NoSuchMethodException, ClassNotFoundException {
        getStubClass(InterfaceZ.class).getDeclaredMethod("unusedMethod");
    }

    @Test
    public void testInnerClass() throws ClassNotFoundException, NoSuchFieldException {
        getStubClass(OuterClass.NestedClass.class).getField("x");
        getStubClass(OuterClass.InnerClass.class).getField("y");
    }

    @Test(expectedExceptions = NoSuchFieldException.class)
    public void testUnusedFieldRemoved() throws ClassNotFoundException, NoSuchFieldException {
        getStubClass(OuterClass.NestedClass.class).getField("y");
    }

    @Test(expectedExceptions = NoSuchFieldException.class)
    public void testUnusedFieldRemoved2() throws ClassNotFoundException, NoSuchFieldException {
        getStubClass(OuterClass.InnerClass.class).getField("x");
    }

    @Test
    public void testEnum() throws ClassNotFoundException {
        Class<?> enumClass = getStubClass(MyEnum.class);
        Enum[] constants = (Enum[])enumClass.getEnumConstants();
        int n = constants.length;
        MyEnum[] originalConstants = MyEnum.values();
        assertEquals(n, originalConstants.length);
        for (int i = 0; i<n; i++) {
            assertEquals(constants[i].name(), originalConstants[i].name());
        }
    }

    private Class<?> getStubClass(Class<?> cls) throws ClassNotFoundException {
        return stubClassLoader.loadClass(cls.getName());
    }

    private URLClassLoader getNonTestCL() {
        URLClassLoader testCl = (URLClassLoader) getClass().getClassLoader();
        return new URLClassLoader(testCl.getURLs(), testCl.getParent()) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                for (Class libClass : LIB_CLASSES) {
                    if (libClass.getName().equals(name)) {
                        throw new ClassNotFoundException();
                    }
                }
                for (Class testClass : TEST_CLASSES) {
                    if (testClass.getName().equals(name)) {
                        throw new ClassNotFoundException();
                    }
                }
                return super.findClass(name);
            }
        };
    }

    private File createJar(Class... classes) throws IOException {
        File file = File.createTempFile("test", ".jar");
        ZipOutputStream jar = new ZipOutputStream(new FileOutputStream(file));
        try {
            for (Class cls : classes) {
                String fileName = cls.getName().replace(".", "/") + ".class";
                jar.putNextEntry(new ZipEntry(fileName));
                InputStream in = getClass().getClassLoader().getResourceAsStream(fileName);
                if (in == null) {
                    throw new IllegalArgumentException("No such class file: " + fileName);
                }
                try {
                    IOUtils.copy(in, jar);
                } finally {
                    in.close();
                }
            }
            return file;
        } finally {
            jar.close();
        }
    }
}
