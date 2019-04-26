/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen;

import com.beust.jcommander.JCommander;
import com.maxifier.mxcache.asm.*;
import com.maxifier.mxcache.asm.Type;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * StubGen generates API stubs from binary third-party JARs.
 * <p>
 * It accepts jar file with tested app and a set of jar library files for which to generate API.
 * <p>
 * The output stubs are written to current working directory.
 * Also it takes "copyright" and "comment" files from current directory.
 * These files are injected into source code.
 * <p>
 * Usage: StubGen [options]
 * Options:
 * -c, --comment
 *   Path to file with top comment for all files; by default StubGen seeks for
 *   "comment" file in output path
 * -p, --copyright
 *   Path to file with copyright comment to be added before package statement;
 *   by default StubGen seeks for "copyright" file in output path
 * -e, --examine
 *   Path to JARs to examine
 * -i, --indent
 *   Indentation
 *   Default: "     "
 * -l, --lib
 *   Path to library JARs that StubGen should generate stubs for
 * -o, --o
 *   Output path, current directory by default
 *   Default: .
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-04 08:28)
 */
public class StubGen {
    private final String indent;
    private final boolean redundantModifiers;

    private ClassLoader classLoaderWithApi;

    /**
     * { type internal name : class description } mapping
     */
    private Map<String, ClassDescription> classes;
    private boolean needInitializationStub;
    private Queue<ClassDescription> pendingClasses = new LinkedList<ClassDescription>();

    public StubGen(String indent, boolean redundantModifiers) {
        this.indent = indent;
        this.redundantModifiers = redundantModifiers;
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        Options options = new Options();
        new JCommander(options, args);

        URL[] urls = new URL[options.examine.size() + options.libraries.size()];

        int i = 0;
        for (String examinePath : options.examine) {
            urls[i++] = toUrl(examinePath);
        }
        for (String libraryPath : options.libraries) {
            urls[i++] = toUrl(libraryPath);
        }
        File outputPath = options.outputPath == null ? new File(".") : new File(options.outputPath);
        File commentFile = options.commentPath == null ? new File(outputPath, "comment") : new File(options.commentPath);
        File copyrightFile = options.copyrightPath == null ? new File(outputPath, "copyright") : new File(options.copyrightPath);
        new StubGen(options.indent, options.redundantModifiers).generateStubs(options.examine, urls, outputPath, commentFile, copyrightFile);
    }

    private static URL toUrl(String arg) throws MalformedURLException {
        if (arg.matches("\\w+:/")) {
            return new URL(arg);
        }
        return new URL("file:" + (arg.startsWith("/") ? "" : "/") + arg);
    }

    private void generateStubs(List<String> examinedJars, URL[] libraryJars, File outputDirectory, File commentFile, File copyrightFile) throws IOException, ClassNotFoundException, NoSuchFieldException, NoSuchMethodException {
        String copyright = copyrightFile.exists() ? FileUtils.readFileToString(copyrightFile) : null;
        String comment = commentFile.exists() ? FileUtils.readFileToString(commentFile) : null;

        classes = new THashMap<String, ClassDescription>();
        classLoaderWithApi = new URLClassLoader(libraryJars, getClass().getClassLoader());

        for (String examinedJar : examinedJars) {
            JarFile jar = new JarFile(new File(examinedJar));
            try {
                for (JarEntry jarEntry : Collections.list(jar.entries())) {
                    if (jarEntry.getName().endsWith(".class")) {
                        ClassReader r = new ClassReader(IOUtils.toByteArray(jar.getInputStream(jarEntry)));
                        r.accept(new ClassVisitor(Opcodes.ASM7) {
                            Type thisType;

                            @Override
                            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                thisType = Type.getObjectType(name);
                                ClassDescription t = typeUsed(thisType);
                                if (t != null) {
                                    t.implemented = true;
                                }
                                if (superName != null) {
                                    typeUsed(Type.getObjectType(superName));
                                }
                                if (interfaces != null) {
                                    for (String anInterface : interfaces) {
                                        typeUsed(Type.getObjectType(anInterface));
                                    }
                                }
                            }

                            @Override
                            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                                fieldUsed(thisType, name, desc);
                                return null;
                            }

                            @Override
                            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                                methodUsed(thisType, name, desc);
                                return new MethodVisitor(Opcodes.ASM7) {
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
            } finally {
                jar.close();
            }
        }

        while (!pendingClasses.isEmpty()) {
            ClassDescription classDescription = pendingClasses.poll();
            if (classDescription == null || classDescription.enclosingClass != null || classDescription.implemented) {
                continue;
            }
            classDescription.prepare();

            Class<?> realClass = classDescription.realClass;

            File out = new File(outputDirectory, realClass.getName().replace('.', '/') + ".java");
            //noinspection ResultOfMethodCallIgnored
            out.getParentFile().mkdirs();

            ImportTable imports = new ImportTable(realClass.getPackage());

            classDescription.addImports(imports);

            PrintWriter w = new PrintWriter(out);
            try {
                if (copyright != null) {
                    w.println(copyright);
                }
                w.println("package " + realClass.getPackage().getName() + ";");
                w.println();

                for (String importedClass : imports.getSortedImports()) {
                    w.println("import " + importedClass + ";");
                }
                if (!imports.getSortedImports().isEmpty()) {
                    w.println();
                }

                if (comment != null) {
                    w.println(comment);
                }
                classDescription.printDefinition(imports, w, "");
            } finally {
                w.close();
            }
        }
        if (needInitializationStub) {
            File out = new File(outputDirectory, "stub/InitializationStub.java");
            //noinspection ResultOfMethodCallIgnored
            out.getParentFile().mkdirs();

            PrintWriter w = new PrintWriter(out);
            try {
                if (copyright != null) {
                    w.println(copyright);
                }
                w.println("package stub;");
                w.println();
                w.println("/** THIS IS AUTOGENERATED STUFF FOR FINAL FIELD INITIALIZATION */");
                if (comment != null) {
                    w.println(comment);
                }
                w.println("public class InitializationStub {");

                w.println(indent + "public static boolean getBoolean() {");
                w.println(indent + indent + "return false;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static byte getByte() {");
                w.println(indent + indent + "return (byte)0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static char getCharacter() {");
                w.println(indent + indent + "return 'X';");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static short getShort() {");
                w.println(indent + indent + "return (short)0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static int getInt() {");
                w.println(indent + indent + "return 0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static long getLong() {");
                w.println(indent + indent + "return 0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static float getFloat() {");
                w.println(indent + indent + "return 0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static double getDouble() {");
                w.println(indent + indent + "return 0;");
                w.println(indent + "}");
                w.println();

                w.println(indent + "public static <T> T get(Class<?> c) {");
                w.println(indent + indent + "return null;");
                w.println(indent + "}");

                w.println("}");
            } finally {
                w.close();
            }
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
                    Constructor<?> ctor = classDescription.realClass.getDeclaredConstructor(argClasses);
                    classDescription.usedConstructors.add(ctor);
                    genericTypeUsed(ctor.getTypeParameters());
                    genericTypeUsed(ctor.getGenericParameterTypes());
                    genericTypeUsed(ctor.getGenericExceptionTypes());
                } else {
                    try {
                        for (Method method : unbridge(classDescription.realClass.getDeclaredMethod(name, argClasses))) {
                            classDescription.usedMethods.add(method);

                            genericTypeUsed(method.getTypeParameters());
                            genericTypeUsed(method.getGenericParameterTypes());
                            genericTypeUsed(method.getGenericExceptionTypes());
                            genericTypeUsed(method.getGenericReturnType());
                        }

                    } catch (NoSuchMethodException ignored) {
                    }
                    Class<?> superClass = classDescription.realClass.getSuperclass();
                    if (superClass != null && superClass != Object.class && superClass != Enum.class) {
                        methodUsed(Type.getType(superClass), name, desc);
                    }
                    for (Class<?> implementedInterface : classDescription.realClass.getInterfaces()) {
                        methodUsed(Type.getType(implementedInterface), name, desc);
                    }
                }
            } catch (ClassNotFoundException ignored) {
            } catch (NoClassDefFoundError ignored) {
            } catch (NoSuchMethodException ignored) {
            }
        } else {
            for (Type argType : Type.getArgumentTypes(desc)) {
                typeUsed(argType);
            }
            typeUsed(Type.getReturnType(desc));
        }
    }

    // we can't know for sure which method is bridged, but we will try to guess based on argument types
    private static Collection<Method> unbridge(Method declaredMethod) {
        if (!declaredMethod.isBridge()) {
            return Collections.singleton(declaredMethod);
        }
        Class<?>[] bridgeParams = declaredMethod.getParameterTypes();
        List<Method> possibleUnbridged = new ArrayList<Method>();
        outer: for (Method method : declaredMethod.getDeclaringClass().getDeclaredMethods()) {
            if (!method.isBridge() && method.getName().equals(declaredMethod.getName()) && method.getParameterTypes().length == declaredMethod.getParameterTypes().length) {
                Class<?>[] params = method.getParameterTypes();
                for (int i = 0; i<params.length; i++) {
                    if (!bridgeParams[i].isAssignableFrom(params[i])) {
                        continue outer;
                    }
                }
                possibleUnbridged.add(method);
            }
        }
        return possibleUnbridged;
    }

    private void fieldUsed(Type type, String name, String desc) {
        typeUsed(Type.getType(desc));
        ClassDescription classDescription = typeUsed(type);
        if (classDescription != null) {
            try {
                Field field = classDescription.realClass.getDeclaredField(name);
                if (!field.isEnumConstant()) {
                    classDescription.usedFields.add(field);
                    genericTypeUsed(field.getGenericType());
                }
            } catch (NoClassDefFoundError ignored) {
            } catch (NoSuchFieldException ignored) {
            } catch (IncompatibleClassChangeError ignored) {
            }
        }
    }

    private void genericTypeUsed(java.lang.reflect.Type... genericTypes) {
        for (java.lang.reflect.Type type : genericTypes) {
            genericTypeUsed(type);
        }
    }

    private void genericTypeUsed(java.lang.reflect.Type genericType) {
        genericTypeUsed(new THashSet<java.lang.reflect.Type>(), genericType);
    }

    private void genericTypeUsed(Set<java.lang.reflect.Type> visited, java.lang.reflect.Type genericType) {
        if (!visited.add(genericType)) {
            return;
        }
        if (genericType instanceof Class) {
            typeUsed(Type.getType((Class) genericType));
        } else if (genericType instanceof GenericArrayType) {
            GenericArrayType gt = (GenericArrayType) genericType;
            genericTypeUsed(visited, gt.getGenericComponentType());
        } else if (genericType instanceof TypeVariable) {
            TypeVariable tv = (TypeVariable) genericType;
            for (java.lang.reflect.Type bound : tv.getBounds()) {
                genericTypeUsed(visited, bound);
            }
        } else if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            genericTypeUsed(visited, pt.getRawType());
            java.lang.reflect.Type[] actualTypeArguments = pt.getActualTypeArguments();
            for (java.lang.reflect.Type actualTypeArgument : actualTypeArguments) {
                genericTypeUsed(visited, actualTypeArgument);
            }
        } else if (genericType instanceof WildcardType) {
            WildcardType wt = (WildcardType) genericType;
            for (java.lang.reflect.Type upper : wt.getUpperBounds()) {
                genericTypeUsed(visited, upper);
            }
            for (java.lang.reflect.Type aLower : wt.getLowerBounds()) {
                genericTypeUsed(visited, aLower);
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
                pendingClasses.add(v);

                Class<?> enclosingRealClass = realClass.getEnclosingClass();
                if (enclosingRealClass != null) {
                    ClassDescription enclosingClass = typeUsed(Type.getType(enclosingRealClass));
                    if (enclosingClass != null) {
                        v.enclosingClass = enclosingClass;
                        enclosingClass.innerClasses.add(v);
                    }
                }

                for (TypeVariable<?> typeVariable : realClass.getTypeParameters()) {
                    genericTypeUsed(typeVariable);
                }
                Class<?> superClass = realClass.getSuperclass();
                if (superClass != null && superClass != Object.class) {
                    typeUsed(Type.getType(superClass));
                    genericTypeUsed(realClass.getGenericSuperclass());
                }
                for (Class<?> implementedInterface : realClass.getInterfaces()) {
                    typeUsed(Type.getType(implementedInterface));
                }
                for (java.lang.reflect.Type genericInterface : realClass.getGenericInterfaces()) {
                    genericTypeUsed(genericInterface);
                }

                return v;
            }
        } catch (NoClassDefFoundError ignored) {
        } catch (ClassNotFoundException ignored) {
        } catch (ClassFormatError ignored) {
        }
        classes.put(name, null);
        return null;
    }

    class ClassDescription {
        final Class<?> realClass;
        boolean implemented;

        Set<Method> usedMethods = new THashSet<Method>();
        Set<Method> inheritedUsedMethods = new THashSet<Method>();
        Set<Constructor> usedConstructors = new THashSet<Constructor>();
        Set<Field> usedFields = new THashSet<Field>();

        Constructor bestSuperCtor;

        ClassDescription enclosingClass;
        List<ClassDescription> innerClasses = new ArrayList<ClassDescription>();
        private boolean requiresExplicitConstructor;

        ClassDescription(Class<?> realClass) {
            this.realClass = realClass;
        }

        private void addImports(ImportTable imports) {
            imports.add(realClass);
            imports.add(realClass.getSuperclass());
            imports.add(realClass.getInterfaces());

            imports.add(realClass.getGenericSuperclass());
            imports.add(realClass.getGenericInterfaces());

            imports.add(realClass.getTypeParameters());

            for (Field field : usedFields) {
                imports.add(field.getType());

                imports.add(field.getGenericType());
            }

            for (Method method : usedMethods) {
                imports.add(method.getParameterTypes());
                imports.add(method.getExceptionTypes());
                imports.add(method.getReturnType());

                imports.add(method.getGenericParameterTypes());
                imports.add(method.getGenericExceptionTypes());
                imports.add(method.getGenericReturnType());
            }

            for (Method method : calculateInheritedUsedMethods()) {
                imports.add(method.getParameterTypes());
                imports.add(method.getExceptionTypes());
                imports.add(method.getReturnType());

                imports.add(method.getGenericParameterTypes());
                imports.add(method.getGenericExceptionTypes());
                imports.add(method.getGenericReturnType());
            }

            for (Constructor ctor : usedConstructors) {
                imports.add(ctor.getParameterTypes());
                imports.add(ctor.getExceptionTypes());

                imports.add(ctor.getGenericParameterTypes());
                imports.add(ctor.getGenericExceptionTypes());
            }

            for (ClassDescription innerClass : innerClasses) {
                innerClass.addImports(imports);
            }
            imports.forceSimpleName(realClass);
            for (ClassDescription innerClass : innerClasses) {
                imports.forceSimpleName(innerClass.realClass);
            }
        }

        private void printDefinition(ImportTable imports, PrintWriter w, String baseIndent) {
            int classModifiers = realClass.getModifiers();
            if (!redundantModifiers && Modifier.isInterface(classModifiers)) {
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

            printTypeVariablesDefinition(imports, w, realClass.getTypeParameters());

            Class<?> superClass = realClass.getSuperclass();
            if (superClass != null && superClass != Object.class && superClass != Enum.class) {
                w.print(" extends ");
                printGenericType(imports, w, realClass.getGenericSuperclass());
            }
            java.lang.reflect.Type[] genericInterfaces = realClass.getGenericInterfaces();
            if (genericInterfaces.length != 0) {
                w.print(realClass.isInterface() ? " extends " : " implements ");
                for (int i = 0; i < genericInterfaces.length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    printGenericType(imports, w, genericInterfaces[i]);
                }
            }

            w.println(" {");

            if (realClass.isEnum()) {
                w.print(baseIndent + indent);
                Enum[] constants = (Enum[]) realClass.getEnumConstants();
                // print all constants to keep the ordinals
                for (int i = 0; i < constants.length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    w.print(constants[i].name());
                }
                if (!usedFields.isEmpty() || !usedConstructors.isEmpty() || !usedMethods.isEmpty() || !innerClasses.isEmpty() || requiresExplicitConstructor) {
                    w.println(";");
                } else {
                    w.println();
                }
            }

            for (Field field : usedFields) {
                printField(imports, w, baseIndent, field);
            }

            if (requiresExplicitConstructor) {
                w.println(baseIndent + indent + "// Default constructor, generated because there was no feasible super ctor");
                w.println(baseIndent + indent + "protected " + imports.getImportedName(realClass) + "() {");
                printInvokeSuperCtor(w, baseIndent + indent, bestSuperCtor);
                w.println(baseIndent + indent + "}");
            }

            for (Constructor ctor : usedConstructors) {
                printCtor(imports, w, baseIndent, ctor, bestSuperCtor);
            }

            for (Method method : usedMethods) {
                printMethod(imports, w, baseIndent, method);
            }

            if (!inheritedUsedMethods.isEmpty()) {
                w.println(baseIndent + indent + "// Methods inherited from parent");
                for (Method method : inheritedUsedMethods) {
                    printMethod(imports, w, baseIndent, method);
                }
            }

            for (ClassDescription innerClass : innerClasses) {
                innerClass.printDefinition(imports, w, baseIndent + indent);
            }

            w.println(baseIndent + "}");
        }

        private void printInvokeSuperCtor(PrintWriter w, String baseIndent, Constructor bestSuperCtor) {
            w.print(baseIndent + indent + "super(");
            Class[] paramTypes = bestSuperCtor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                if (i > 0) {
                    w.print(", ");
                }
                w.print(getDefaultValue(paramTypes[i]));

            }
            w.println(");");
        }

        private String getDefaultValue(Class argType) {
            if (argType == boolean.class) {
                return "false";
            } else if (argType == byte.class) {
                return "(byte)0";
            } else if (argType == short.class) {
                return "(short)0";
            } else if (argType == int.class) {
                return "0";
            } else if (argType == long.class) {
                return "0L";
            } else if (argType == float.class) {
                return "0F";
            } else if (argType == double.class) {
                return "0D";
            } else if (argType == char.class) {
                return "'\0'";
            } else {
                return "null";
            }
        }

        private Set<Method> calculateInheritedUsedMethods() {
            Set<Method> res = new THashSet<Method>();
            addInheritedUsedMethods(res);
            return res;
        }

        private void addInheritedUsedMethods(Set<Method> res) {
            Class<?> superClass = realClass.getSuperclass();
            if (superClass != null) {
                addInheritedUsedMethods(res, superClass);
            }
            for (Class<?> implementedInterface : realClass.getInterfaces()) {
                addInheritedUsedMethods(res, implementedInterface);
            }
        }

        private void addInheritedUsedMethods(Set<Method> res, Class<?> type) {
            Method[] methods;
            ClassDescription typeDescription = classes.get(Type.getInternalName(type));
            if (typeDescription == null) {
                methods = type.getDeclaredMethods();
            } else {
                methods = typeDescription.usedMethods.toArray(new Method[typeDescription.usedMethods.size()]);
            }
            for (Method method : methods) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    try {
                        for (Method ownMethod : unbridge(realClass.getDeclaredMethod(method.getName(), method.getParameterTypes()))) {
                            if (!usedMethods.contains(ownMethod)) {
                                res.add(ownMethod);
                            }
                        }
                    } catch (NoSuchMethodException ignored) {
                    }
                }
            }
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                addInheritedUsedMethods(res, superClass);
            }
            for (Class<?> implementedInterface: type.getInterfaces()){
                addInheritedUsedMethods(res, implementedInterface);
            }
        }

        private void printField(ImportTable imports, PrintWriter w, String baseIndent, Field field) {
            w.print(baseIndent + indent);
            w.print(Modifier.toString(field.getModifiers()));
            w.print(" ");
            w.print(imports.getImportedName(field.getType()));
            w.print(" " + field.getName());
            if (Modifier.isFinal(field.getModifiers())) {
                needInitializationStub = true;
                if (field.getType().isPrimitive()) {
                    w.print(" = stub.InitializationStub.get" + capitalize(field.getType().getSimpleName()) + "()");
                } else {
                    // no generics, it will be inferred.
                    w.print(" = stub.InitializationStub.get(" + imports.getImportedName(field.getType()) + ".class)");
                }
            }
            w.println(";");
        }

        private String capitalize(String name) {
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }

        private void printCtor(ImportTable imports, PrintWriter w, String baseIndent, Constructor ctor, Constructor bestSuperCtor) {
            w.print(baseIndent + indent + Modifier.toString(ctor.getModifiers()) + " ");

            if (printTypeVariablesDefinition(imports, w, ctor.getTypeParameters())) {
                w.print(" ");
            }

            w.print(realClass.getSimpleName());

            printParameterList(imports, w, ctor.getGenericParameterTypes());
            printThrowsList(imports, w, ctor.getGenericExceptionTypes());
            if (bestSuperCtor != null && bestSuperCtor.getParameterTypes().length > 0) {
                w.println("{");
                printInvokeSuperCtor(w, baseIndent + indent, bestSuperCtor);
                w.println(baseIndent + indent + "}");
            } else {
                w.println("{}");
            }
            w.println();
        }

        private void printMethod(ImportTable imports, PrintWriter w, String baseIndent, Method method) {
            w.print(baseIndent + indent);

            int methodModifiers = method.getModifiers();
            // VOLATILE = BRIDGE
            methodModifiers &= ~Modifier.VOLATILE;

            // TRANSIENT = VARARG
            methodModifiers &= ~Modifier.TRANSIENT;

            if (!redundantModifiers && realClass.isInterface()) {
                methodModifiers &= ~Modifier.ABSTRACT;
                methodModifiers &= ~Modifier.PUBLIC;
            }
            String modifiersString = Modifier.toString(methodModifiers);
            if (!modifiersString.isEmpty()) {
                w.print(modifiersString + " ");
            }

            if (printTypeVariablesDefinition(imports, w, method.getTypeParameters())) {
                w.print(" ");
            }

            printGenericType(imports, w, method.getGenericReturnType());

            w.print(" " + method.getName());
            printParameterList(imports, w, method.getGenericParameterTypes());
            printThrowsList(imports, w, method.getGenericExceptionTypes());

            if (Modifier.isAbstract(method.getModifiers())) {
                w.println(";");
            } else {
                Class<?> returnType = method.getReturnType();
                if (returnType == void.class) {
                    w.println(" {}");
                } else {
                    w.println(" {");
                    w.println(baseIndent + indent + indent + "return " + getDefaultValue(returnType) + ";");
                    w.println(baseIndent + indent + "}");
                }
            }
            w.println();
        }

        private void printGenericType(ImportTable imports, PrintWriter w, java.lang.reflect.Type genericType) {
            if (genericType instanceof Class) {
                w.print(imports.getImportedName((Class) genericType));
            } else if (genericType instanceof GenericArrayType) {
                GenericArrayType gt = (GenericArrayType) genericType;
                printGenericType(imports, w, gt.getGenericComponentType());
                w.print("[]");
            } else if (genericType instanceof TypeVariable) {
                TypeVariable tv = (TypeVariable) genericType;
                w.print(tv.getName());
            } else if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                printGenericType(imports, w, pt.getRawType());
                w.print("<");
                java.lang.reflect.Type[] actualTypeArguments = pt.getActualTypeArguments();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    java.lang.reflect.Type arg = actualTypeArguments[i];
                    printGenericType(imports, w, arg);
                }
                w.print(">");
            } else if (genericType instanceof WildcardType) {
                WildcardType wt = (WildcardType) genericType;
                w.print("?");


                java.lang.reflect.Type[] lower = wt.getLowerBounds();
                if (lower.length > 0) {
                    w.print(" super ");
                    for (int i = 0; i < lower.length; i++) {
                        if (i > 0) {
                            w.print("&");
                        }
                        printGenericType(imports, w, lower[i]);
                    }
                }
                java.lang.reflect.Type[] uppers = filterUpperBounds(wt.getUpperBounds());
                if (uppers.length > 0) {
                    w.print(" extends ");
                    for (int i = 0; i < uppers.length; i++) {
                        if (i > 0) {
                            w.print("&");
                        }
                        printGenericType(imports, w, uppers[i]);
                    }
                }
            } else {
                w.print(genericType);
            }
        }

        private boolean printTypeVariablesDefinition(ImportTable imports, PrintWriter w, TypeVariable<?>[] typeVariables) {
            if (typeVariables.length > 0) {
                w.print("<");
                for (int i = 0; i < typeVariables.length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    TypeVariable<?> var = typeVariables[i];

                    w.print(var.getName());
                    java.lang.reflect.Type[] bounds = var.getBounds();
                    if (bounds.length > 0 && (bounds.length != 1 || bounds[0] != Object.class)) {
                        w.print(" extends ");
                        for (int j = 0; j < bounds.length; j++) {
                            if (j > 0) {
                                w.print(" & ");
                            }
                            printGenericType(imports, w, bounds[j]);
                        }
                    }
                }
                w.print(">");
                return true;
            }
            return false;
        }

        private void printThrowsList(ImportTable imports, PrintWriter w, java.lang.reflect.Type[] exceptionTypes) {
            if (exceptionTypes != null && exceptionTypes.length > 0) {
                w.print(" throws ");
                for (int i = 0; i < exceptionTypes.length; i++) {
                    if (i > 0) {
                        w.print(", ");
                    }
                    printGenericType(imports, w, exceptionTypes[i]);
                }
            }
        }

        private void printParameterList(ImportTable imports, PrintWriter w, java.lang.reflect.Type[] genericParameterTypes) {
            w.print("(");
            for (int i = 0; i < genericParameterTypes.length; i++) {
                if (i > 0) {
                    w.print(", ");
                }
                printGenericType(imports, w, genericParameterTypes[i]);
                w.print(" p" + (i + 1));
            }
            w.print(")");
        }

        private void prepare() {
            if (realClass.isInterface() || Modifier.isAbstract(realClass.getModifiers())) {
                inheritedUsedMethods = Collections.emptySet();
            } else {
                inheritedUsedMethods = calculateInheritedUsedMethods();
                for (Method method : inheritedUsedMethods) {
                    genericTypeUsed(method.getTypeParameters());
                    genericTypeUsed(method.getGenericParameterTypes());
                    genericTypeUsed(method.getGenericExceptionTypes());
                    genericTypeUsed(method.getGenericReturnType());
                }
            }
            Class<?> superClass = realClass.getSuperclass();
            if (superClass != null && !realClass.isEnum()) {
                Constructor<?>[] ctors;
                ClassDescription superDesc = classes.get(Type.getInternalName(superClass));
                if (superDesc == null) {
                    ctors = superClass.getDeclaredConstructors();
                } else {
                    ctors = superDesc.usedConstructors.toArray(new Constructor[superDesc.usedConstructors.size()]);
                }
                for (Constructor<?> ctor : ctors) {
                    int ctorModifiers = ctor.getModifiers();
                    if (Modifier.isPublic(ctorModifiers) || Modifier.isProtected(ctorModifiers) || (!Modifier.isPrivate(ctorModifiers) && superClass.getPackage().equals(realClass.getPackage()))) {
                        if (bestSuperCtor == null || bestSuperCtor.getParameterTypes().length > ctor.getParameterTypes().length) {
                            bestSuperCtor = ctor;
                        }
                    }
                }
            }
            requiresExplicitConstructor = usedConstructors.isEmpty() && bestSuperCtor != null && bestSuperCtor.getParameterTypes().length > 0;

            for (ClassDescription innerClass : innerClasses) {
                innerClass.prepare();
            }
        }
    }

    private static java.lang.reflect.Type[] filterUpperBounds(java.lang.reflect.Type[] upperBounds) {
        java.lang.reflect.Type[] res = new java.lang.reflect.Type[upperBounds.length];
        int n = 0;
        for (java.lang.reflect.Type type : upperBounds) {
            if (type != Object.class) {
                res[n++] = type;
            }
        }
        return n == upperBounds.length ? upperBounds : Arrays.copyOf(res, n);
    }
}
