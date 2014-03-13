/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.asm.Opcodes;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.asm.commons.Method;
import com.maxifier.mxcache.util.*;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

import static com.maxifier.mxcache.util.CodegenHelper.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class MxGenericProxyFactory<T, C> extends MxAbstractProxyFactory {
    private static final Logger logger = LoggerFactory.getLogger(MxGenericProxyFactory.class);

    private static final Type MX_GENERIC_PROXY_TYPE = Type.getType(MxGenericProxy.class);
    private static final Method MX_GENERIC_PROXY_CTOR = new Method(CONSTRUCTOR_NAME, Type.VOID_TYPE, new Type[]{RESOLVABLE_TYPE, CLASS_TYPE, CLASS_TYPE, CLASS_TYPE});

    private final Class<T> sourceInterface;
    private final Class<C> containerClass;

    private final Map<Class<? extends T>, Constructor<T>> proxyClasses = new THashMap<Class<? extends T>, Constructor<T>>();

    MxGenericProxyFactory(@Nonnull Class<T> sourceInterface, @Nonnull Class<C> containerClass) {
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
     * <p>
     * Creates proxy-object that implements given interface {@link #getSourceInterface()} and all its child interfaces
     * of passed type. It is not guaranteed that proxy will extend this class if it is not interface.
     * Proxy stores a link to container of {@link #getContainerClass()} type. The proxy class has two constructors:
     * default (for serialization) and single parameter-constructor {@link #getContainerClass()}.
     * <p/>
     * <p>
     * Proxy overrides {@code toString()} method that returns toString() of stored object from container.
     * </p>
     * <p>
     * Proxies for the same container with same target interface and value from container will be considered equal
     * in terms of equals and hashCode.
     * </p>
     *
     * @param srcClass this type will be used in order to add all child interfaces of getSourceInterface() to proxy
     * @param container container of proxyed objects
     * @return created proxy
     */
    public T createProxy(Class<? extends T> srcClass, C container) {
        try {
            return getOrCreateProxy(srcClass).newInstance(container);
        } catch (Exception e) {
            throw new RuntimeException("Cannot create proxy for " + container, e);
        }
    }

    private final class MethodInfo {
        private final Class[] argTypes;
        private final Class returnType;
        private final String name;
        private final int hash;

        MethodInfo(Class[] argTypes, Class returnType, String name) {
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
            return hash == info.hash &&
                    returnType == info.returnType &&
                    name.equals(info.name) &&
                    Arrays.equals(argTypes, info.argTypes);
        }
    }

    private Class<T> createProxyClass(Class<? extends T> sourceClass, Class<C> containerClass) {
        long start = System.currentTimeMillis();
        try {
            Set<Class<? extends T>> interfaces = getAllProxiedInterfaces(sourceClass);

            String proxyClassName = createProxyClassName(sourceClass);
            Type containerType = Type.getType(containerClass);

            ClassGenerator proxyClass = new ClassGenerator(Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, proxyClassName, MxGenericProxy.class, interfaces.toArray(new Class[interfaces.size()]));

            createProxyConstructor(sourceClass, containerClass, proxyClass, containerType);

            createMethodProxies(containerClass, interfaces, proxyClass, containerType);

            //noinspection unchecked
            Class<T> newClass = proxyClass.toClass(sourceClass.getClassLoader());

            long end = System.currentTimeMillis();

            logger.debug("Generated generic proxy for " + sourceClass + "[" + sourceInterface + ", " + interfaces.size() + "] with container " + containerClass + " in " + (end - start) + " ms");

            return newClass;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createMethodProxies(Class<C> containerClass, Set<Class<? extends T>> interfaces, ClassGenerator generator, Type containerType) {
        Set<MethodInfo> methods = new THashSet<MethodInfo>();

        for (Class<? extends T> intf : interfaces) {
            for (java.lang.reflect.Method sourceMethod : intf.getMethods()) {
                if (sourceMethod.getDeclaringClass().equals(intf)) {
                    Class<?> returnType = sourceMethod.getReturnType();
                    Class<?>[] args = sourceMethod.getParameterTypes();

                    if (methods.add(new MethodInfo(args, returnType, sourceMethod.getName()))) {
                        createProxyMethod(containerClass, generator, containerType, intf, sourceMethod);
                    }
                }
            }
        }
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
        //noinspection ManualArrayToCollectionCopy
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

    private void createProxyMethod(Class<C> containerClass, ClassGenerator generator, Type containerType, Class<? extends T> intf, java.lang.reflect.Method sourceMethod) {
        Method sourceMethod0 = Method.getMethod(sourceMethod);
        MxGeneratorAdapter method = generator.defineMethod(Opcodes.ACC_PUBLIC, sourceMethod0);
        method.start();

        method.loadThis();
        method.getField(MX_GENERIC_PROXY_TYPE, VALUE_FIELD_NAME, RESOLVABLE_TYPE);
        method.checkCast(containerType);
        method.invokeVirtual(Type.getType(containerClass), GETTER);
        method.loadArgs();
        method.invokeInterface(Type.getType(intf), sourceMethod0);
        method.returnValue();
        method.endMethod();
    }

    private void createProxyConstructor(Class<? extends T> sourceClass, Class<C> containerClass, ClassGenerator generator, Type containerType) {
        MxConstructorGenerator ctor = generator.defineConstructor(Opcodes.ACC_PUBLIC, containerType);
        ctor.start();
        ctor.loadThis();
        ctor.loadArg(0);
        ctor.push(Type.getType(sourceClass));
        ctor.push(Type.getType(containerClass));
        ctor.push(Type.getType(sourceInterface));
        ctor.invokeConstructor(MX_GENERIC_PROXY_TYPE, MX_GENERIC_PROXY_CTOR);
        ctor.returnValue();
        ctor.endMethod();
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