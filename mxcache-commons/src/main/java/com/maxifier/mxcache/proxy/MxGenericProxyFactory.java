package com.maxifier.mxcache.proxy;

import gnu.trove.THashMap;
import gnu.trove.THashSet;
import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 12:35:39
 */
public final class MxGenericProxyFactory<T, C> extends MxAbstractProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(MxGenericProxyFactory.class);

    private static final String VALUE_FIELD_NAME = "value";
    private static final String SUPER_CLASS_NAME = MxGenericProxy.class.getName();

    private final Class<T> sourceInterface;
    private final Class<C> containerClass;

    private final Map<Class<? extends T>, Constructor<T>> proxyClasses = new THashMap<Class<? extends T>, Constructor<T>>();

    MxGenericProxyFactory(@NotNull Class<T> sourceInterface, @NotNull Class<C> containerClass) {
        if (!sourceInterface.isInterface()) {
            throw new IllegalArgumentException("Only interface can be used as proxy source but " + sourceInterface + " was passed");
        }
        if (!Resolvable.class.isAssignableFrom(containerClass)) {
            throw new IllegalArgumentException("Container " + containerClass + " should implement " + Resolvable.class);
        }
        this.sourceInterface = sourceInterface;
        this.containerClass = containerClass;
    }

    private synchronized Constructor<? extends T> getOrCreateProxy(Class<? extends T> cls) {
        Constructor<T> proxyInfo = proxyClasses.get(cls);
        if (proxyInfo == null) {
            Class<T> proxyClass = createProxyClass(cls, containerClass);
            try {
                proxyInfo = proxyClass.getConstructor(containerClass);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            proxyClasses.put(cls, proxyInfo);
        }
        return proxyInfo;
    }

    public Class<T> getSourceInterface() {
        return sourceInterface;
    }

    public Class<?> getContainerClass() {
        return containerClass;
    }

    /**
     * Создает прокси-объект, который реализует заданный интерфейс {@link #getSourceInterface()} а также все его
     * интерфейсы-наследники, которые реализовал исходный класс, хранит внутри ссылку на контейнер типа
     * {@link #getContainerClass()}, имеет один конструктор с одним параметром типа {@link #getContainerClass()}.
     * <p/>
     * Прокси имеет переопределенный метод toString() который возврящает строку, состоящую из toString() хранимого
     * в контейнере объекта, hashCode и equals, сравнивающие классы контейнера, целевой интерфейс и значение из
     * контейнера. <b>Прокси, созданные для разных типов <code>srcClass</code> считаются различными!!!</b>
     * <p><b>Прокси нельзя кастить к классу элемента, а только к некоторому общему интерфейсу!!!</b></p>
     *
     * @param srcClass  класс элемента
     * @param container контейнер
     * @return прокси
     */
    public T createProxy(Class<? extends T> srcClass, C container) {
        try {
            return getOrCreateProxy(srcClass).newInstance(container);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create proxy for " + container, e);
        }
    }

    private final class MethodInfo {
        private final Type[] argTypes;
        private final Type returnType;
        private final String name;
        private final int hash;

        MethodInfo(Type[] argTypes, Type returnType, String name) {
            this.argTypes = argTypes;
            this.returnType = returnType;
            this.name = name;
            hash = Arrays.hashCode(argTypes) ^ returnType.hashCode() ^ name.hashCode();
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @SuppressWarnings({"unchecked"})
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            MethodInfo info = (MethodInfo) obj;
            return hash == info.hash && Arrays.equals(argTypes, info.argTypes) &&
                    returnType.equals(info.returnType) && name.equals(info.name);
        }
    }

    private Class<T> createProxyClass(Class<? extends T> sourceClass, Class<C> containerClass) {
        long start = System.currentTimeMillis();
        try {
            Set<Class<? extends T>> interfaces = getAllProxiedInterfaces(sourceClass);

            String proxyClassName = createProxyClassName(sourceClass);
            Type containerType = Type.getType(containerClass);

            ConstantPoolGen cpg = new ConstantPoolGen();
            ClassGen proxyClass = new ClassGen(proxyClassName, SUPER_CLASS_NAME, "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, getInterfaceNames(interfaces), cpg);

            InstructionFactory factory = new InstructionFactory(proxyClass);

            proxyClass.addMethod(createProxyConstructor(sourceClass, containerClass, cpg, proxyClassName, SUPER_CLASS_NAME, containerType, factory));

            createMethodProxies(containerClass, interfaces, cpg, proxyClassName, SUPER_CLASS_NAME, containerType, proxyClass, factory);

            //noinspection unchecked
            Class<T> newClass = (Class<T>) loadClass(sourceClass, proxyClass);

            long end = System.currentTimeMillis();

            logger.debug("Generated generic proxy for " + sourceClass + "[" + sourceInterface + ", " + interfaces.size() + "] with container " + containerClass + " in " + (end - start) + " ms");

            return newClass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createMethodProxies(Class<C> containerClass, Set<Class<? extends T>> interfaces, ConstantPoolGen cpg, String proxyClassName, String superClassName, Type containerType, ClassGen proxyClass, InstructionFactory factory) {
        Set<MethodInfo> methods = new THashSet<MethodInfo>();

        for (Class<? extends T> intf : interfaces) {
            for (Method sourceMethod : intf.getMethods()) {
                if (sourceMethod.getDeclaringClass().equals(intf)) {
                    Type returnType = Type.getType(sourceMethod.getReturnType());
                    Type[] argTypes = getTypes(sourceMethod.getParameterTypes());

                    if (methods.add(new MethodInfo(argTypes, returnType, sourceMethod.getName()))) {
                        proxyClass.addMethod(createProxyMethod(containerClass, cpg, proxyClassName, superClassName, containerType, factory, intf, sourceMethod, returnType, argTypes));
                    }
                }
            }
        }
    }

    private Type[] getTypes(Class[] params) {
        Type[] argTypes = new Type[params.length];
        for (int i = 0; i < params.length; i++) {
            argTypes[i] = Type.getType(params[i]);
        }
        return argTypes;
    }

    private String[] getInterfaceNames(Set<Class<? extends T>> interfaces) {
        String[] interfaceNames = new String[interfaces.size()];
        int iid = 0;
        for (Class<? extends T> anInterface : interfaces) {
            interfaceNames[iid++] = anInterface.getName();
        }
        return interfaceNames;
    }

    private Set<Class<? extends T>> getAllProxiedInterfaces(Class<? extends T> sourceClass) {
        Set<Class<? extends T>> interfaces = new THashSet<Class<? extends T>>();
        Queue<Class> iq = new LinkedList<Class>();
        Set<Class> passed = new THashSet<Class>();
        iq.add(sourceClass);
        if (sourceClass.isInterface()) {
            //noinspection unchecked
            interfaces.add(sourceClass);
        }
        for (Class cls : sourceInterface.getInterfaces()) {
            //noinspection unchecked
            interfaces.add(cls);
        }
        while (!iq.isEmpty()) {
            Class c = iq.poll();
            if (c.getSuperclass() != null && passed.add(c.getSuperclass())) {
                iq.add(c.getSuperclass());
            }
            for (Class<?> cls : c.getInterfaces()) {
                if (passed.add(cls)) {
                    iq.add(cls);
                }
                if (sourceInterface.isAssignableFrom(cls)) {
                    //noinspection unchecked
                    interfaces.add((Class<T>) cls);
                }
            }
        }
        return interfaces;
    }

    private org.apache.bcel.classfile.Method createProxyMethod(Class<C> containerClass, ConstantPoolGen cpg, String proxyClassName, String superClassName, Type containerType, InstructionFactory factory, Class<? extends T> intf, Method sourceMethod, Type returnType, Type[] argTypes) {
        InstructionList methodCode = new InstructionList();
        methodCode.append(InstructionFactory.createLoad(containerType, 0));
        methodCode.append(factory.createGetField(superClassName, VALUE_FIELD_NAME, RESOLVABLE_TYPE));
        methodCode.append(factory.createCast(Type.OBJECT, containerType));
        methodCode.append(factory.createInvoke(containerClass.getName(), GETTER_NAME, Type.OBJECT, EMPTY_TYPES, Constants.INVOKEVIRTUAL));
        for (int i = 0; i < argTypes.length; i++) {
            methodCode.append(InstructionFactory.createLoad(argTypes[i], i + 1));
        }
        methodCode.append(factory.createInvoke(intf.getName(), sourceMethod.getName(), returnType, argTypes, Constants.INVOKEINTERFACE));
        methodCode.append(InstructionFactory.createReturn(returnType));

        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, returnType, argTypes, null, sourceMethod.getName(), proxyClassName, methodCode, cpg);
        method.setMaxStack(argTypes.length + 2);

        return method.getMethod();
    }

    private org.apache.bcel.classfile.Method createProxyConstructor(Class<? extends T> sourceClass, Class<C> containerClass, ConstantPoolGen cpg, String proxyClassName, String superClassName, Type containerType, InstructionFactory factory) {
        InstructionList constructorBody = new InstructionList();
        constructorBody.append(InstructionFactory.createLoad(containerType, 0));
        constructorBody.append(InstructionFactory.createLoad(containerType, 1));
        constructorBody.append(factory.createConstant(sourceClass.getName()));
        constructorBody.append(factory.createInvoke(Class.class.getName(), "forName", Type.CLASS, new Type[]{ Type.STRING}, Constants.INVOKESTATIC));
        constructorBody.append(factory.createConstant(containerClass.getName()));
        constructorBody.append(factory.createInvoke(Class.class.getName(), "forName", Type.CLASS, new Type[]{ Type.STRING}, Constants.INVOKESTATIC));
        constructorBody.append(factory.createConstant(sourceInterface.getName()));
        constructorBody.append(factory.createInvoke(Class.class.getName(), "forName", Type.CLASS, new Type[]{ Type.STRING}, Constants.INVOKESTATIC));
        constructorBody.append(factory.createInvoke(superClassName, Constants.CONSTRUCTOR_NAME, Type.VOID, new Type[]{ RESOLVABLE_TYPE, Type.CLASS, Type.CLASS, Type.CLASS}, Constants.INVOKESPECIAL));
        constructorBody.append(InstructionFactory.createReturn(Type.VOID));

        MethodGen constructor = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, new Type[]{containerType}, new String[]{VALUE_FIELD_NAME}, Constants.CONSTRUCTOR_NAME, proxyClassName, constructorBody, cpg);

        constructor.setMaxStack(5);

        return constructor.getMethod();
    }

    @Override
    public String toString() {
        return "ProxyFactory for " + sourceInterface + " wrapped in " + containerClass;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MxGenericProxyFactory)) {
            return false;
        }

        MxGenericProxyFactory that = (MxGenericProxyFactory) o;
        return containerClass == that.containerClass && sourceInterface == that.sourceInterface;

    }

    @Override
    public int hashCode() {
        return 31 * sourceInterface.hashCode() + containerClass.hashCode();
    }
}