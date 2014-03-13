/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (30.05.13)
 */
class NodeWrapperFactoryImpl implements NodeWrapperFactory {
    private final Constructor<? extends AbstractDependencyNode> constructor;

    public NodeWrapperFactoryImpl(Constructor<? extends AbstractDependencyNode> constructor) {
        this.constructor = constructor;
    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public AbstractDependencyNode wrap() {
        try {
            return constructor.newInstance();
        } catch (InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
    }
}
