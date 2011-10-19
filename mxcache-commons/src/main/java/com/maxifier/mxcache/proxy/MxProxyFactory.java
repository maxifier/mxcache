package com.maxifier.mxcache.proxy;

import org.apache.bcel.Constants;
import org.apache.bcel.generic.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.01.2009
 * Time: 12:35:39
 */
public final class MxProxyFactory<T, C extends Resolvable<T>> extends MxAbstractProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(MxProxyGenerator.class);

    private static final String VALUE_FIELD_NAME = "value";

    private final Class<T> sourceInterface;
    private final Class<C> containerClass;

    private volatile Constructor<T> proxyConstructor;
    private static final String SUPER_CLASS_NAME = MxAbstractProxy.class.getName();

    MxProxyFactory(@NotNull Class<T> sourceInterface, @NotNull Class<C> containerClass) {
        if (!sourceInterface.isInterface()) {
            throw new IllegalArgumentException("Only interface can be used as proxy source but " + sourceInterface + " was passed");
        }
        if (!Resolvable.class.isAssignableFrom(containerClass)) {
            throw new IllegalArgumentException("Container " + containerClass + " should implement " + Resolvable.class);
        }
        this.sourceInterface = sourceInterface;
        this.containerClass = containerClass;
    }

    private synchronized void initProxy() {
        if (proxyConstructor == null) {
            Class<T> proxyClass = createProxyClass(sourceInterface, containerClass);
            try {
                proxyConstructor = proxyClass.getConstructor(containerClass);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Class<T> getSourceInterface() {
        return sourceInterface;
    }

    public Class<?> getContainerClass() {
        return containerClass;
    }

    /**
     * Создает прокси-объект, который реализует заданный интерфейс {@link #getSourceInterface()}, хранит внутри ссылку
     * на контейнер типа {@link #getContainerClass()}, имеет два конструктора: по умолчанию (для сериализации), и
     * с одним параметром типа {@link #getContainerClass()}.
     * <p/>
     * Прокси имеет переопределенный метод toString() который возврящает строку, состоящую из toString() хранимого
     * в контейнере объекта, hashCode и equals, сравнивающие классы контейнера, целевой интерфейс и значение из
     * контейнера.
     *
     * @param container контейнер
     * @return прокси
     */
    public T createProxy(C container) {
        try {
            return getProxyConstructor().newInstance(container);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create proxy for " + container, e);
        }
    }

    private synchronized Constructor<T> getProxyConstructor() {
        if (proxyConstructor == null) {
            initProxy();
        }
        return proxyConstructor;
    }

    private static <T, C extends Resolvable<T>> Class<T> createProxyClass(Class<T> sourceClass, Class<C> containerClass) {
        long start = System.currentTimeMillis();
        try {
            String proxyClassName = createProxyClassName(sourceClass);
            Type containerType = Type.getType(containerClass);

            ConstantPoolGen cpg = new ConstantPoolGen();
            ClassGen proxyClass = new ClassGen(proxyClassName, SUPER_CLASS_NAME, "<generated>", Constants.ACC_PUBLIC | Constants.ACC_SUPER, new String[]{sourceClass.getName()}, cpg);

            InstructionFactory factory = new InstructionFactory(proxyClass);

            proxyClass.addMethod(createProxyConstructor(sourceClass, containerClass, cpg, proxyClassName, containerType, factory));

            for (Method sourceMethod : sourceClass.getMethods()) {
                proxyClass.addMethod(createMethodProxy(sourceClass, containerClass, cpg, proxyClassName, containerType, factory, sourceMethod));
            }

            proxyClass.addEmptyConstructor(Constants.ACC_PUBLIC);

            return loadClass(sourceClass, proxyClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();

            logger.debug("Generated proxy for " + sourceClass + " with container " + containerClass + " in " + (end - start) + " ms");
        }
    }

    private static <T, C extends Resolvable<T>> org.apache.bcel.classfile.Method createProxyConstructor(Class<T> sourceClass, Class<C> containerClass, ConstantPoolGen cpg, String proxyClassName, Type containerType, InstructionFactory factory) {
        InstructionList constructorBody = new InstructionList();
        constructorBody.append(InstructionFactory.createLoad(containerType, 0));
        constructorBody.append(InstructionFactory.createLoad(containerType, 1));
        constructorBody.append(factory.createConstant(sourceClass.getName()));
        constructorBody.append(factory.createInvoke(Class.class.getName(), "forName", Type.CLASS, new Type[]{Type.STRING}, Constants.INVOKESTATIC));
        constructorBody.append(factory.createConstant(containerClass.getName()));
        constructorBody.append(factory.createInvoke(Class.class.getName(), "forName", Type.CLASS, new Type[]{Type.STRING}, Constants.INVOKESTATIC));
        constructorBody.append(factory.createInvoke(SUPER_CLASS_NAME, Constants.CONSTRUCTOR_NAME, Type.VOID, new Type[]{ RESOLVABLE_TYPE, Type.CLASS, Type.CLASS}, Constants.INVOKESPECIAL));
        constructorBody.append(InstructionFactory.createReturn(Type.VOID));

        MethodGen constructor = new MethodGen(Constants.ACC_PUBLIC, Type.VOID, new Type[]{containerType}, new String[]{VALUE_FIELD_NAME}, Constants.CONSTRUCTOR_NAME, proxyClassName, constructorBody, cpg);

        constructor.setMaxStack(4);
        return constructor.getMethod();
    }

    private static <T, C extends Resolvable<T>> org.apache.bcel.classfile.Method createMethodProxy(Class<T> sourceClass, Class<C> containerClass, ConstantPoolGen cpg, String proxyClassName, Type containerType, InstructionFactory factory, Method sourceMethod) {
        Type returnType = Type.getType(sourceMethod.getReturnType());

        Class[] params = sourceMethod.getParameterTypes();
        Type[] argTypes = new Type[params.length];

        for (int i = 0; i < params.length; i++) {
            argTypes[i] = Type.getType(params[i]);
        }

        InstructionList methodCode = new InstructionList();
        methodCode.append(InstructionFactory.createLoad(containerType, 0));
        methodCode.append(factory.createGetField(SUPER_CLASS_NAME, VALUE_FIELD_NAME, RESOLVABLE_TYPE));
        methodCode.append(factory.createCast(Type.OBJECT, containerType));
        methodCode.append(factory.createInvoke(containerClass.getName(), GETTER_NAME, Type.OBJECT, EMPTY_TYPES, Constants.INVOKEVIRTUAL));
        for (int i = 0; i < params.length; i++) {
            methodCode.append(InstructionFactory.createLoad(argTypes[i], i + 1));
        }
        methodCode.append(factory.createInvoke(sourceClass.getName(), sourceMethod.getName(), returnType, argTypes, Constants.INVOKEINTERFACE));
        methodCode.append(InstructionFactory.createReturn(returnType));

        MethodGen method = new MethodGen(Constants.ACC_PUBLIC, returnType, argTypes, null, sourceMethod.getName(), proxyClassName, methodCode, cpg);
        method.setMaxStack(params.length + 2);
        return method.getMethod();        
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
        if (!(o instanceof MxProxyFactory)) {
            return false;
        }

        MxProxyFactory that = (MxProxyFactory) o;
        return containerClass == that.containerClass && sourceInterface == that.sourceInterface;

    }

    @Override
    public int hashCode() {
        return 31 * sourceInterface.hashCode() + containerClass.hashCode();
    }

}
