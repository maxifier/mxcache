package com.maxifier.mxcache.impl.resource;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 28.02.12
 * Time: 21:40
 *
 * No equals or hash code: each resource is unique!
 */
class MxBoundResource extends MxResourceImpl {

    public MxBoundResource(@NotNull Object owner, @NotNull String name) {
        super(owner, name);
    }

    @Override
    public String toString() {
        return owner + "#" + getName();
    }
}
