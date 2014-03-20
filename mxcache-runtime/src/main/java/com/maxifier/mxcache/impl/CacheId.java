/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CacheId {
    @Nonnull
    private final Class owner;

    private final int id;

    public CacheId(@Nonnull Class owner, int id) {
        this.owner = owner;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CacheId cacheId = (CacheId) o;

        return id == cacheId.id && owner == cacheId.owner;
    }

    @Override
    public int hashCode() {
        return 31 * owner.hashCode() + id;
    }

    @Override
    public String toString() {
        return "CacheId{owner=" + owner + ", id=" + id + '}';
    }
}
