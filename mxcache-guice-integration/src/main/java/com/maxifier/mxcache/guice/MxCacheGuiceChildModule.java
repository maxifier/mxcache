/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guice;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.Scopes;
import com.maxifier.mxcache.PublicAPI;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.util.ClassGenerator;
import gnu.trove.map.hash.THashMap;
import javax.annotation.Nullable;

import java.util.Map;

import static com.maxifier.mxcache.asm.Opcodes.ACC_PUBLIC;

/**
 * MxCacheGuiceChildModule
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxCacheGuiceChildModule extends AbstractModule {
    private static final Map<Class<? extends CacheContext>, Class<? extends GuiceInstanceProvider>> CACHE = new THashMap<Class<? extends CacheContext>, Class<? extends GuiceInstanceProvider>>();

    private final Class contextClass;

    private final Class<? extends GuiceInstanceProvider> instanceProviderClass;

    private final String name;

    @PublicAPI
    // this method may be used by client
    public MxCacheGuiceChildModule(Class<? extends CacheContext> contextClass) {
        this(contextClass, null);
    }

    public MxCacheGuiceChildModule(Class<? extends CacheContext> contextClass, @Nullable String name) {
        this.contextClass = contextClass;
        this.name = name;
        this.instanceProviderClass = getInstanceProviderClass(contextClass);
    }

    private static Class<? extends GuiceInstanceProvider> getInstanceProviderClass(Class<? extends CacheContext> contextClass) {
        if (contextClass == CacheContext.class) {
            return GuiceInstanceProvider.class;
        }
        if (!CacheContext.class.isAssignableFrom(contextClass)) {
            throw new IllegalArgumentException("Child CacheContext interface should extends CacheContext: " + contextClass);
        }
        if (!contextClass.isInterface()) {
            throw new IllegalArgumentException("Child CacheContext should be interface: " + contextClass);
        }
        if (contextClass.getDeclaredMethods().length > 0) {
            throw new IllegalArgumentException("Child CacheContext interface should have no methods: " + contextClass);
        }

        synchronized(CACHE) {
            Class<? extends GuiceInstanceProvider> result = CACHE.get(contextClass);
            if (result == null) {
                //noinspection unchecked
                result = generateClass(contextClass);
                CACHE.put(contextClass, result);
            }
            return result;
        }
    }

    private static Class generateClass(Class<? extends CacheContext> contextClass) {
        String name = Type.getInternalName(contextClass) + "$GuiceInstanceProvider";
        return new ClassGenerator(ACC_PUBLIC, name, GuiceInstanceProvider.class, contextClass)
                .defineDefaultConstructor()
                .toClass(contextClass.getClassLoader());
    }

    @SuppressWarnings( { "unchecked" })
    @Override
    protected void configure() {
        install(new PrivateModule() {
            @Override
            protected void configure() {
                if (name != null) {
                    bindConstant().annotatedWith(GuiceInstanceProviderName.class).to(name);
                }
                bind(instanceProviderClass).in(Scopes.SINGLETON);
                expose(instanceProviderClass);
            }
        });
        bind(contextClass).to(instanceProviderClass);
    }
}
