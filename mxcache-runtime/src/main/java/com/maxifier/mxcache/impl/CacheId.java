package com.maxifier.mxcache.impl;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 10.03.2010
 * Time: 9:26:48
 */
public final class CacheId {
    @NotNull
    private final Class owner;

    private final int id;

    public CacheId(@NotNull Class owner, int id) {
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
