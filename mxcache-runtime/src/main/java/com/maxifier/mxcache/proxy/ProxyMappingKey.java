/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.util.CodegenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
final class ProxyMappingKey {
    private static final Logger logger = LoggerFactory.getLogger(ProxyMappingKey.class);

    private final Class owner;
    private final String name;
    private final String desc;

    ProxyMappingKey(Method method) {
        owner = method.getDeclaringClass();
        name = method.getName();
        desc = Type.getMethodDescriptor(method);
    }

    ProxyMappingKey(Class owner, String name, String desc) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProxyMappingKey that = (ProxyMappingKey) o;
        return owner == that.owner && name.equals(that.name) && desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
        int result = owner.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + desc.hashCode();
        return result;
    }

    public Method getMethod() {
        try {
            return owner.getDeclaredMethod(name, CodegenHelper.getClasses(owner.getClassLoader(), Type.getArgumentTypes(desc)));
        } catch (NoSuchMethodException e) {
            logger.error("Cannot create proxy factory for " + this, e);
            return null;
        }
    }

    @Override
    public String toString() {
        return owner.getCanonicalName()+ "." + name + desc;
    }
}
