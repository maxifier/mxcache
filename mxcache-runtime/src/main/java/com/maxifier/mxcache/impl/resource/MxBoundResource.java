/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import javax.annotation.Nonnull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
