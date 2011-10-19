package com.maxifier.mxcache.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.maxifier.mxcache.InstanceProvider;
import com.maxifier.mxcache.context.CacheContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.10.2010
 * Time: 13:54:18
 */
@Singleton
public class GuiceInstanceProvider implements InstanceProvider, CacheContext {
    private Injector injector;
    private String name;

    @Inject(optional = true)
    public void setName(@GuiceInstanceProviderName String name) {
        this.name = name;
    }

    @Inject
    public void setInjector(Injector injector) {
        this.injector = injector;
    }

    @NotNull
    @Override
    public <T> T forClass(@NotNull Class<T> cls) {
        return injector.getInstance(cls);
    }

    @Override
    public InstanceProvider getInstanceProvider() {
        return this;
    }

    @Override
    public String toString() {
        return name == null ? super.toString() : name;
    }
}
