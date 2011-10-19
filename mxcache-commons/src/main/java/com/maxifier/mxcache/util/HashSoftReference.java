package com.maxifier.mxcache.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 15:03:54
 */
public class HashSoftReference<T> extends SoftReference<T> {
    private int hashCode;

    public HashSoftReference(@NotNull T referent) {
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
            // если у нас уже нет возможности сравнить сами объекты, то считаем, что все ссылки вдруг стали неравны.
            // это не должно порождать броблем, т.к. hashCode у них не меняется, и рефлексивность проверяется раньше.
            return false;
        }
        T thatObject = that.get();
        if (thatObject == null) {
            return false;
        }

        return thisObject.equals(thatObject);
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
