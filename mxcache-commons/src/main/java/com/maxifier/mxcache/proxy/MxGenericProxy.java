package com.maxifier.mxcache.proxy;

import javax.annotation.Nonnull;

import java.io.Serializable;

public class MxGenericProxy<T, C extends Resolvable<T>> extends MxProxy<T, C> implements Serializable {
    @Nonnull
    protected final C value;
    private final Class<T> mainInterface;
    private final Class<T> valueClass;
    private final Class<C> containerClass;

    protected MxGenericProxy(@Nonnull C value, Class<T> valueClass, Class<C> containerClass, Class<T> mainInterface) {
        this.value = value;
        this.valueClass = valueClass;
        this.containerClass = containerClass;
        this.mainInterface = mainInterface;
    }

    @Override
    public String toString() {
        return "GenericProxy for <" + value.getValue() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MxGenericProxy)) {
            return false;
        }

        MxGenericProxy that = (MxGenericProxy) o;
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
        return new WriteProxy<T, C>(value, valueClass, containerClass, mainInterface);
    }

    @Nonnull
    @Override
    public C getValue() {
        return value;
    }

    //------------------------------------------------------------------------------------------------------------------

    private static final class WriteProxy<T, C extends Resolvable<T>> implements Serializable {
        private static final long serialVersionUID = 1285165L;

        @Nonnull
        private final C container;
        private final Class<T> mainInterface;
        private final Class<T> valueClass;
        private final Class<C> containerClass;

        public WriteProxy(@Nonnull C container, Class<T> valueClass, Class<C> containerClass, Class<T> mainInterface) {
            this.container = container;
            this.valueClass = valueClass;
            this.containerClass = containerClass;
            this.mainInterface = mainInterface;
        }

        public Object readResolve() {
            return MxProxyGenerator.getGenericProxyFactory(mainInterface, containerClass).createProxy(valueClass, container);
        }
    }
}