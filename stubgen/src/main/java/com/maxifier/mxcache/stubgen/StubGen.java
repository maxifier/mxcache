/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.Type;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * APIFier generates API stubs from binary third-party JARs.
 *
 * It accepts jar file with tested app and a set of jar library files for which to generate API.
 *
 * The output stubs are written to current working directory.
 * Also it takes "copyright" and "comment" files from current directory.
 * These files are injected into source code.
 *
 * Usage:
 * APIfier {tested jar} {library jar 1} {library jar 2} ... {library jar N}
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-04 08:28)
 */
public class StubGen {
    public static final String INDENT = "     ";
    public static final File COPYRIGHT_FILE = new File("copyright");
    public static final File COMMENT_FILE = new File("comment");

    private ClassLoader classLoaderWithApi;
    /** { type internal name : class description } mapping */
    private Map<String, ClassDescription> classes;

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        String testedJarPath = args[0];
        URL[] urls = new URL[args.length];
        for (int i = 0; i < urls.length; i++) {
            String arg = args[i];
            if (arg.matches("\\w+:/")) {
                urls[i] = new URL(arg);
            } else {
                urls[i] = new URL("file:" + (arg.startsWith("/") ? "" : "/") + arg);
            }
        }
        new StubGen().apify(testedJarPath, urls);
    }

    private void apify(String testedJarPath, URL[] urls) throws IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        String copyright = COPYRIGHT_FILE.exists() ? FileUtils.readFileToString(COPYRIGHT_FILE) : "";
        String comment = COMMENT_FILE.exists() ? FileUtils.readFileToString(COMMENT_FILE) : "";

        classes = new THashMap<String, ClassDescription>();
        classLoaderWithApi = new URLClassLoader(urls, getClass().getClassLoader());

        classLoaderWithApi.loadClass("com.intellij.openapi.util.UserDataHolder");

        JarFile jar = new JarFile(new File(testedJarPath));
        for (JarEntry jarEntry : Collections.list(jar.entries())) {
            if (jarEntry.getName().endsWith(".class")) {
                ClassReader r = new ClassReader(IOUtils.toByteArray(jar.getInputStream(jarEntry)));
                r.accept(new ClassVisitor(Opcodes.ASM5) {
                    Type superType;
                    Type[] interfaces;

                    @Override
                    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                        ClassDescription t = typeUsed(Type.getObjectType(name));
                        if (t != null) {
                            t.implemented = true;
                        }
                        if (superName != null) {
                            superType = Type.getObjectType(superName);
                            typeUsed(superType);
                        }
                        this.interfaces = new Type[interfaces == null ? 0 : interfaces.length];
                        if (interfaces != null) {
                            for (int i = 0; i < interfaces.length; i++) {
                                Type interfaceType = Type.getObjectType(interfaces[i]);
                                typeUsed(interfaceType);
                                this.interfaces[i] = interfaceType;
                            }
                        }
                    }

                    @Override
                    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                        typeUsed(Type.getType(desc));
                        return null;
                    }

                    @Override
                    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                        if (superType != null) {
                            methodUsed(superType, name, desc);
                        }
                        for (Type implementedInterface : interfaces) {
                            methodUsed(implementedInterface, name, desc);
                        }
                        processMethodSignature(desc);
                        return new MethodVisitor(Opcodes.ASM5) {
                            @Override
                            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                                methodUsed(Type.getObjectType(owner), name, desc);
                            }

                            @Override
                            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                                fieldUsed(Type.getObjectType(owner), name, desc);
                            }
                        };
                    }
                }, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
            }
        }

        for (ClassDescription classDescription : classes.values()) {
            if (classDescription == null || classDescription.enclosingClass != null || classDescription.implemented) {
                continue;
            }
            Class<?> realClass = classDescription.realClass;

            File out = new File(realClass.getName().replace('.', '/') + ".java");
            //noinspection ResultOfMethodCallIgnored
            out.getParentFile().mkdirs();

            ImportTable imports = new ImportTable(realClass.getPackage());

            classDescription.addImports(imports);

            PrintWriter w = new PrintWriter(out);
            try {
                w.println(copyright);
                w.println("package " + realClass.getPackage().getName() + ";");
                w.println();

                Set<String> sortedImports = new TreeSet<String>();
                for (Class importedClass : imports.values()) {
                    sortedImports.add(importedClass.getCanonicalName());
                }

                for (String importedClass : sortedImports) {
                    w.println("import " + importedClass + ";");
                }
                if (!sortedImports.isEmpty()) {
                    w.println();
                }

                w.println(comment);
                classDescription.printDefinition(imports, w, "");
            } finally {
                w.close();
            }
        }
    }

    private static void printThrowsList(ImportTable imports, PrintWriter w, Class<?>[] exceptionTypes) {
        if (exceptionTypes != null && exceptionTypes.length > 0) {
            w.print(" throws ");
            for (int i = 0; i<exceptionTypes.length; i++) {
                if (i > 0) {
                    w.print(", ");
                }
                w.print(imports.getImportedName(exceptionTypes[i]));
            }
        }
    }

    private static void printParameterList(ImportTable imports, PrintWriter w, Class<?>[] parameterTypes, java.lang.reflect.Type[] genericParameterTypes) {
        w.print("(");
        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) {
                w.print(", ");
            }
            printGenericType(imports, w, parameterTypes[i], genericParameterTypes[i]);
            w.print(" p" + (i + 1));
        }
        w.print(")");
    }

    static class ImportTable extends TreeMap<String, Class> {
        private final Package currentPackage;

        ImportTable(Package currentPackage) {
            this.currentPackage = currentPackage;
        }

        public void add(Class... cs) {
            for (Class c : cs) {
                add(c);
            }
        }

        public void add(Class c) {
            c = resolveImportType(c);
            if (c != null) {
                put(c.getSimpleName(), c);
            }
        }

        public String getImportedName(Class c) {
            if (c.isArray()) {
                return getImportedName(c.getComponentType()) + "[]";
            }
            if (c.isPrimitive()) {
                return c.getCanonicalName();
            }
            if (c.getCanonicalName().startsWith("java.lang.")) {
                return c.getSimpleName();
            }
            if (c.getPackage().equals(currentPackage)) {
                return c.getSimpleName();
            }
            if (get(c.getSimpleName()) == c) {
                return c.getSimpleName();
            }
            return c.getCanonicalName();
        }

        private Class<?> resolveImportType(Class<?> t) {
            if (t == null) {
                return null;
            }
            while (t.isArray()) {
                t = t.getComponentType();
            }
            if (t.isPrimitive()) {
                return null;
            }
            if (t.getCanonicalName().startsWith("java.lang.")) {
                return null;
            }
            if (t.getPackage() == currentPackage) {
                return null;
            }
            return t;
        }
    }

    private Class<?> toClass(Type argument) throws ClassNotFoundException {
        int sort = argument.getSort();
        switch (sort) {
            case Type.BOOLEAN:
                return boolean.class;
            case Type.CHAR:
                return char.class;
            case Type.BYTE:
                return byte.class;
            case Type.SHORT:
                return short.class;
            case Type.INT:
                return int.class;
            case Type.LONG:
                return long.class;
            case Type.FLOAT:
                return float.class;
            case Type.DOUBLE:
                return double.class;
            case Type.ARRAY:
                return Array.newInstance(toClass(argument.getElementType()), 0).getClass();
        }
        return classLoaderWithApi.loadClass(argument.getClassName());
    }

    private void processMethodSignature(String desc) {
        Type[] arguments = Type.getArgumentTypes(desc);
        for (Type type : arguments) {
            typeUsed(type);
        }
        typeUsed(Type.getReturnType(desc));
    }

    private void methodUsed(Type type, String name, String desc) {
        ClassDescription classDescription = typeUsed(type);
        if (classDescription != null) {
            try {
                Type[] arguments = Type.getArgumentTypes(desc);
                Class[] argClasses = new Class[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    argClasses[i] = toClass(arguments[i]);
                }
                if (name.equals("<init>")) {
                    classDescription.usedConstructors.add(classDescription.realClass.getDeclaredConstructor(argClasses));
                } else {
                    try {
                        Method method = classDescription.realClass.getDeclaredMethod(name, argClasses);
                        classDescription.usedMethods.add(method);
                        for (Class<?> exceptionType : method.getExceptionTypes()) {
                            typeUsed(Type.getType(exceptionType));
                        }
                    } catch (NoSuchMethodException e) {
                        Class<?> superClass = classDescription.realClass.getSuperclass();
                        if (superClass != null && superClass != Object.class && superClass != Enum.class) {
                            methodUsed(Type.getType(superClass), name, desc);
                        }
                        for (Class<?> implementedInterface : classDescription.realClass.getInterfaces()) {
                            methodUsed(Type.getType(implementedInterface), name, desc);
                        }
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (NoClassDefFoundError ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        }
        processMethodSignature(desc);
    }

    private void fieldUsed(Type type, String name, String desc) {
        typeUsed(Type.getType(desc));
        ClassDescription classDescription = typeUsed(type);
        if (classDescription != null) {
            try {
                classDescription.usedFields.add(classDescription.realClass.getDeclaredField(name));
            } catch (NoSuchFieldException ignored) {
            } catch (IncompatibleClassChangeError ignored) {
            }
        }
    }

    private ClassDescription typeUsed(Type type) {
        while (type.getSort() == Type.ARRAY) {
            type = type.getElementType();
        }
        if (type.getSort() != Type.OBJECT) {
            // ignore primitives
            return null;
        }
        String name = type.getInternalName();
        if (classes.containsKey(name)) {
            return classes.get(name);
        }
        Class<?> commonClass = null;
        try {
            commonClass = getClass().getClassLoader().loadClass(type.getClassName());
        } catch (ClassNotFoundException ignore) {
        } catch (ClassFormatError ignore) {
        }
        try {
            Class<?> realClass = classLoaderWithApi.loadClass(type.getClassName());

            // we don't want to apify classes from common classloader like java.lang.*
            if (realClass != commonClass) {
                ClassDescription v = new ClassDescription(realClass);
                classes.put(name, v);

                Class<?> enclosingRealClass = realClass.getEnclosingClass();
                if (enclosingRealClass != null) {
                    ClassDescription enclosingClass = typeUsed(Type.getType(enclosingRealClass));
                    if (enclosingClass != null) {
                        v.enclosingClass = enclosingClass;
                        enclosingClass.innerClasses.add(v);
                    }
                }

                Class<?> superClass = realClass.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    typeUsed(Type.getType(superClass));
                }
                for (Class<?> implementedInterface: realClass.getInterfaces()){
                    typeUsed(Type.getType(implementedInterface));
                }

                return v;
            }
        } catch (ClassNotFoundException e) {
//            System.err.println(e.getMessage());
        } catch (ClassFormatError e) {
            // some classes in ASM are broken
//            e.printStackTrace();
        }
        classes.put(name, null);
        return null;
    }

    class ClassDescription {
        final Class<?> realClass;
        boolean implemented;

        Set<Method> usedMethods = new THashSet<Method>();
        Set<Constructor> usedConstructors = new THashSet<Constructor>();
        Set<Field> usedFields = new THashSet<Field>();

        ClassDescription enclosingClass;
        List<ClassDescription> innerClasses = new ArrayList<ClassDescription>();

        ClassDescription(Class<?> realClass) {
            this.realClass = realClass;
        }

        public boolean hasDefaultCtor() {
            for (Constructor ctor : usedConstructors) {
                if (ctor.getParameterTypes().length == 0) {
                    return true;
                }
            }
            return false;
        }

        private void addImports(ImportTable imports) {
            imports.add(realClass);
            imports.add(realClass.getSuperclass());
            imports.add(realClass.getInterfaces());

            for (Field field: usedFields) {
                imports.add(field.getType());
            }

            for (Method method : usedMethods) {
                imports.add(method.getParameterTypes());
                imports.add(method.getExceptionTypes());
                imports.add(method.getReturnType());
            }

            for (Constructor method : usedConstructors) {
                imports.add(method.getParameterTypes());
                imports.add(method.getExceptionTypes());
            }

            for (ClassDescription innerClass : innerClasses) {
                innerClass.addImports(imports);
            }
        }

        private void printDefinition(ImportTable imports, PrintWriter w, String baseIndent) {
            int classModifiers = realClass.getModifiers();
            if (Modifier.isInterface(classModifiers)) {
                classModifiers &= ~Modifier.ABSTRACT;
            }
            if (realClass.isEnum()) {
                classModifiers &= ~Modifier.FINAL;
                classModifiers &= ~Modifier.ABSTRACT;
            }
            w.print(baseIndent + Modifier.toString(classModifiers));
            if (realClass.isInterface()) {
                w.print(" ");
            } else if (realClass.isEnum()) {
                w.print(" enum ");
            } else {
                w.print(" class ");
            }
            w.print(realClass.getSimpleName());

            TypeVariable<?>[] genericParams = realClass.getTypeParameters();
            if (genericParams.length > 0) {
                w.print('<');
                for (int i = 0; i < genericParams.length; i++) {
                    TypeVariable<?> param = genericParams[i];
                    if (i > 0) {
                        w.print(',');
                    }
                    w.print(param.getTypeName());
                }
                w.append('>');
            }
            realClass.toGenericString();

            Class<?> superClass = realClass.getSuperclass();
            if (superClass != null && superClass != Object.class && superClass != Enum.class) {
                w.print(" extends ");
                w.print(imports.getImportedName(superClass));
            }
            if (realClass.getInterfaces().length != 0) {
                w.print(realClass.isInterface() ? " extends " : " implements ");
                for (int i = 0; i < realClass.getInterfaces().length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    w.print(imports.getImportedName(realClass.getInterfaces()[i]));
                }
            }

            w.println(" {");

            if (realClass.isEnum()) {
                boolean first = true;
                w.print(baseIndent + INDENT);
                for (Field field : usedFields) {
                    if (field.isEnumConstant()) {
                        if (first) {
                            first = false;
                        } else {
                            w.print(", ");
                        }
                        w.print(field.getName());
                    }
                }
                w.println(";");
            }

            for (Field field : usedFields) {
                if (!field.isEnumConstant()) {
                    w.print(baseIndent + INDENT);
                    w.print(Modifier.toString(field.getModifiers()));
                    w.print(" ");
                    w.print(imports.getImportedName(field.getType()));
                    w.print(" " + field.getName());
                    if (Modifier.isFinal(field.getModifiers())) {
                        ClassDescription t = typeUsed(Type.getType(field.getType()));
                        if (t != null) {
                            w.print(" = new " + t.realClass.getSimpleName() + "()");
                        }
                    }
                    w.println(";");
                }
            }

            if (!realClass.isInterface() && !realClass.isEnum() && !hasDefaultCtor()) {
                w.println(baseIndent + INDENT + "public " + realClass.getSimpleName() + "() {}");
            }

            for (Constructor ctor : usedConstructors) {
                w.print(baseIndent + INDENT + Modifier.toString(ctor.getModifiers()) + " " + realClass.getSimpleName());

                printParameterList(imports, w, ctor.getParameterTypes(), ctor.getGenericParameterTypes());
                printThrowsList(imports, w, ctor.getExceptionTypes());
                w.println("{}");
                w.println();
            }

            for (Method method : usedMethods) {
                w.print(baseIndent + INDENT + Modifier.toString(method.getModifiers()) + " ");

                TypeVariable<Method>[] typeVariables = method.getTypeParameters();
                if (typeVariables.length != 0) {
                    w.print("<");
                    for (int i = 0; i < typeVariables.length; i++) {
                        if (i > 0) {
                            w.print(", ");
                        }
                        TypeVariable<Method> var = typeVariables[i];
                        w.print(var.getName());
                    }
                    w.print("> ");
                }

                printGenericType(imports, w, method.getReturnType(), method.getGenericReturnType());

                w.print(" " + method.getName());
                printParameterList(imports, w, method.getParameterTypes(), method.getGenericParameterTypes());
                printThrowsList(imports, w, method.getExceptionTypes());

                if (Modifier.isAbstract(method.getModifiers())) {
                    w.println(";");
                } else {
                    w.println(" {");
                    w.println(baseIndent + INDENT + INDENT + "throw new UnsupportedOperationException();");
                    w.println(baseIndent + INDENT + "}");
                }
                w.println();
            }

            for (ClassDescription innerClass : innerClasses) {
                innerClass.printDefinition(imports, w, baseIndent + INDENT);
            }

            w.println(baseIndent + "}");
        }
    }

    private static void printGenericType(ImportTable imports, PrintWriter w, Class basic, java.lang.reflect.Type type) {
        if (type instanceof Class) {
            w.print(imports.getImportedName((Class)type));
        } else if (type instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType)type;
            printGenericType(imports, w, basic.getComponentType(), gt.getGenericComponentType());
            w.print("[]");
        } else if (type instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable)type;
            w.print(tv.getName());
        } else if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)type;
            printGenericType(imports, w, basic, pt.getRawType());
            w.print("<");
            java.lang.reflect.Type[] actualTypeArguments = pt.getActualTypeArguments();
            for (int i = 0; i < actualTypeArguments.length; i++) {
                if (i > 0) {
                    w.print(", ");
                }
                java.lang.reflect.Type arg = actualTypeArguments[i];
                printGenericType(imports, w, Object.class, arg);
            }
            w.print(">");
        } else if (type instanceof WildcardType) {
            WildcardType wt = (WildcardType)type;
            w.print("?");
            java.lang.reflect.Type[] lower = wt.getLowerBounds();
            if (lower.length > 0) {
                w.print(" super ");
                for (int i = 0; i < lower.length; i++) {
                    if (i > 0) {
                        w.print("&");
                    }
                    printGenericType(imports, w, Object.class, lower[i]);
                }
            }
            java.lang.reflect.Type[] uppers = wt.getUpperBounds();
            if (uppers.length > 0) {
                w.print(" extends ");
                for (int i = 0; i < uppers.length; i++) {
                    if (i > 0) {
                        w.print("&");
                    }
                    printGenericType(imports, w, Object.class, uppers[i]);
                }
            }
        } else {
            w.print(imports.getImportedName(basic));
        }
    }
}
