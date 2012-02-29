package com.maxifier.mxcache.impl.resource;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 28.02.12
 * Time: 21:40
 *
 * No equals or hash code: each resource is unique!
 */
class MxBoundResource extends MxResourceImpl implements Serializable {
    private final Object owner;

    public MxBoundResource(@NotNull Object owner, @NotNull String name) {
        super(name);
        this.owner = owner;
    }

    @Override
    public String toString() {
        return owner + "#" + getName();
    }
}
