package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.InstanceProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 18.10.2010
* Time: 11:33:05
*/
class BinderImpl<T> implements Binder<T> {
    private final Class<T> cls;
    private final InstanceProvider instanceProvider;
    private final Map<Class, Provider> registry;

    public BinderImpl(InstanceProvider instanceProvider, Map<Class, Provider> registry, Class<T> cls) {
        this.instanceProvider = instanceProvider;
        this.registry = registry;
        this.cls = cls;
    }

    @Override
    public void toProvider(@NotNull Provider<T> provider) {
        registry.put(cls, provider);
    }

    @Override
    public void toInstance(@NotNull T instance) {
        if (!cls.isInstance(instance)) {
            throw new IllegalArgumentException("Cannot bind " + cls + " to instance of " + instance.getClass());
        }
        registry.put(cls, new ConstProvider<T>(instance));
    }

    @Override
    public void toClass(@NotNull Class<? extends T> cls) {
        if (cls == this.cls) {
            throw new IllegalArgumentException("Cannot bind " + this.cls + " to itself");
        }
        if (!this.cls.isAssignableFrom(cls)) {
            throw new IllegalArgumentException("Cannot bind " + this.cls + " to " + cls);
        }
        registry.put(this.cls, new DelegatingProvider<T>(instanceProvider, cls));
    }
}
