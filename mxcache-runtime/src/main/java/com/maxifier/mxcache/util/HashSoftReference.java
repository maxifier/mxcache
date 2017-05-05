/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import javax.annotation.Nonnull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * A soft reference that has hashCode and equals methods overriden in a way consistent with most hash collections.
 *
 * References will have the same hash code as the original object.
 * They will be considered equal if they reference the same object and this object hasn't been GC'ed yet.
 *
 * Not that when referenced object is GC'ed all references to it will be considered not equal while their hash code will
 * not change.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class HashSoftReference<T> extends SoftReference<T> {
    private int hashCode;

    public HashSoftReference(@Nonnull T referent) {
        super(referent);
        hashCode = hashCode(referent);
    }

    private int hashCode(T referent) {
        return referent == null ? 0 : referent.hashCode();
    }

    public HashSoftReference(T referent, ReferenceQueue<T> q) {
        super(referent, q);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }

        @SuppressWarnings ({ "unchecked" })
        HashSoftReference<T> that = (HashSoftReference<T>) o;

        if (hashCode != that.hashCode) {
            return false;
        }

        T thisObject = get();
        if (thisObject == null) {
            // if we can't compare the actual objects we consider that all references become unequal.
            // This is consistent with most hash maps as hashCode doesn't change and reflectivity of equals
            // is still consistent.
            return false;
        }
        T thatObject = that.get();
        return thatObject != null && thisObject.equals(thatObject);

    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public String toString() {
        Object v = get();
        if (v == null) {
            return "SoftReference<GCed>";
        } else {
            return "SoftReference<" + v + ">";
        }
    }
}
