/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import javax.annotation.Nonnull;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxAbstractProxy<T, C extends Resolvable<T>> extends MxProxy<T, C> implements Serializable {
    @Nonnull
    protected final C value;
    private final Class<T> valueClass;
    private final Class<C> containerClass;

    protected MxAbstractProxy(@Nonnull C value, Class<T> valueClass, Class<C> containerClass) {
        this.value = value;
        this.valueClass = valueClass;
        this.containerClass = containerClass;
    }

    @Override
    public String toString() {
        return "Proxy for <" + value.getValue() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MxAbstractProxy)) {
            return false;
        }

        MxAbstractProxy that = (MxAbstractProxy) o;
        return containerClass == that.containerClass &&
                valueClass == that.valueClass &&
                value.getValue().equals(that.value.getValue());
    }

    @Override
    public int hashCode() {
        int result = value.getValue().hashCode();
        result = 31 * result + valueClass.hashCode();
        result = 31 * result + containerClass.hashCode();
        return result;
    }

    protected Object writeReplace() {
        return new WriteProxy<T, C>(value, valueClass, containerClass);
    }

    @Nonnull
    @Override
    public C getValue() {
        return value;
    }

    //------------------------------------------------------------------------------------------------------------------

    private static final class WriteProxy<T, C extends Resolvable<T>> implements Serializable {
        private static final long serialVersionUID = 43218791L;

        @Nonnull
        private C container;
        private Class<T> valueClass;
        private Class<C> containerClass;

        /**
         * @deprecated for serialization only
         */
        @Deprecated
        public WriteProxy() {
        }

        public WriteProxy(@Nonnull C container, Class<T> valueClass, Class<C> containerClass) {
            this.container = container;
            this.valueClass = valueClass;
            this.containerClass = containerClass;
        }

        public Object readResolve() {
            return MxProxyGenerator.getProxyFactory(valueClass, containerClass).createProxy(container);
        }
    }
}
