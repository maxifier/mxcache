package com.maxifier.mxcache.util;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.lang.ref.ReferenceQueue;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 15:03:54
 */
public class HashWeakReference<T> extends WeakReference<T> {
    private int hashCode;

    public HashWeakReference(@NotNull T referent) {
        super(referent);
        hashCode = hashCode(referent);
    }

    private int hashCode(T referent) {
        return referent == null ? 0 : referent.hashCode();
    }

    public HashWeakReference(T referent, ReferenceQueue<T> q) {
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
        HashWeakReference<T> that = (HashWeakReference<T>) o;

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
            return "WeakReference<GCed>";
        } else {
            return "WeakReference<" + v + ">";
        }
    }
}
