/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import javax.annotation.Nonnull;

import java.io.ObjectStreamException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxStaticResource extends MxResourceImpl {
    public MxStaticResource(@Nonnull String name) {
        super(null, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MxStaticResource that = (MxStaticResource) o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MxResourceSerializableImpl(this);
    }
}
