package com.maxifier.mxcache.impl.resource;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 28.02.12
 * Time: 21:40
 *
 * No equals or hash code: each resource is unique!
 */
class MxBoundResource extends MxResourceImpl {

    public MxBoundResource(@Nonnull Object owner, @Nonnull String name) {
        super(owner, name);
    }

    @Override
    public String toString() {
        return owner + "#" + getName();
    }
}
