/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.ClassGenerator;
import com.maxifier.mxcache.util.MxConstructorGenerator;
import com.maxifier.mxcache.util.MxGeneratorAdapter;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;

import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * MxProxyFactory allows to generate high-throughput lazy-loading proxies.
 * @param <T> type of interface/class for which this factory constructs proxies for
 * @param <C> resolver type that resolves instances of T
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class MxProxyFactory<T, C extends Resolvable<T>> extends MxAbstractProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(MxProxyGenerator.class);

    private static final Method MX_ABSTRACT_PROXY_CTOR = new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, new Type[]{RESOLVABLE_TYPE, CLASS_TYPE, CLASS_TYPE});
    private static final Type MX_ABSTRACT_PROXY_TYPE = Type.getType(MxAbstractProxy.class);

    private final Class<T> sourceInterface;
    private final Class<C> containerClass;

    private volatile Constructor<T> proxyConstructor;

    MxProxyFactory(@Nonnull Class<T> sourceInterface, @Nonnull Class<C> containerClass) {
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
     * <p>
     * Creates proxy-object that implements given interface {@link #getSourceInterface()} and stores a link to
     * container of {@link #getContainerClass()} type. The proxy class has two constructors: default (for serialization)
     * and single parameter-constructor {@link #getContainerClass()}.
     * </p><p>
     * Proxy overrides {@code toString()} method that returns toString() of stored object from container.
     * </p><p>
     * Proxies for the same container with same target interface and value from container will be considered equal
     * in terms of equals and hashCode.
     * </p>
     *
     * @param container container of proxyed objects
     * @return created proxy
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
            Type containerType = Type.getType(containerClass);

            ClassGenerator proxyClass = new ClassGenerator(Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, createProxyClassName(sourceClass), MxAbstractProxy.class, sourceClass);
            createProxyConstructor(sourceClass, containerClass, proxyClass, containerType);
            for (java.lang.reflect.Method sourceMethod : sourceClass.getMethods()) {
                createMethodProxy(sourceClass, proxyClass, containerType, sourceMethod);
            }
            proxyClass.defineDefaultConstructor();

            return proxyClass.toClass(sourceClass.getClassLoader());
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            long end = System.currentTimeMillis();

            logger.debug("Generated proxy for " + sourceClass + " with container " + containerClass + " in " + (end - start) + " ms");
        }
    }

    private static <T, C extends Resolvable<T>> void createProxyConstructor(Class<T> sourceClass, Class<C> containerClass, ClassGenerator generator, Type containerType) {
        MxConstructorGenerator ctor = generator.defineConstructor(Opcodes.ACC_PUBLIC, containerType);
        ctor.start();
        ctor.loadThis();
        ctor.loadArg(0);
        ctor.push(Type.getType(sourceClass));
        ctor.push(Type.getType(containerClass));
        ctor.invokeConstructor(MX_ABSTRACT_PROXY_TYPE, MX_ABSTRACT_PROXY_CTOR);
        ctor.returnValue();
        ctor.endMethod();
    }

    private static <T> void createMethodProxy(Class<T> sourceClass, ClassGenerator cpg, Type containerType, java.lang.reflect.Method sourceMethod) {
        Method sourceMethod0 = Method.getMethod(sourceMethod);
        MxGeneratorAdapter method = cpg.defineMethod(Opcodes.ACC_PUBLIC, sourceMethod0);

        method.start();
        method.loadThis();
        method.getField(MX_ABSTRACT_PROXY_TYPE, VALUE_FIELD_NAME, RESOLVABLE_TYPE);
        method.checkCast(containerType);
        method.invokeVirtual(containerType, GETTER);
        for (int i = 0; i < sourceMethod.getParameterTypes().length; i++) {
            method.loadArg(i);
        }
        method.invokeInterface(Type.getType(sourceClass), sourceMethod0);
        method.returnValue();
        method.endMethod();
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
