package com.maxifier.mxcache.impl.resource;

import org.jetbrains.annotations.NotNull;

import java.io.ObjectStreamException;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 28.02.12
 * Time: 21:44
 */
public class MxStaticResource extends MxResourceImpl {
    public MxStaticResource(@NotNull String name) {
        super(name);
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
