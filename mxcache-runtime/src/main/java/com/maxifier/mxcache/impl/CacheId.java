/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CacheId {
    @Nonnull
    private final Class declaringClass;

    private final int id;

    public CacheId(@Nonnull Class declaringClass, int id) {
        this.declaringClass = declaringClass;
        this.id = id;
    }

    @Nonnull
    public Class getDeclaringClass() {
        return declaringClass;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CacheId cacheId = (CacheId) o;

        return id == cacheId.id && declaringClass.equals(cacheId.declaringClass);

    }

    @Override
    public int hashCode() {
        int result = declaringClass.hashCode();
        result = 31 * result + id;
        return result;
    }

    @Override
    public String toString() {
        return "CacheId{declaringClass=" + declaringClass +  ", id=" + id +  '}';
    }
}
