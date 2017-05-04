/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class MappingIterable<F, T> implements Iterable<T> {
    private final Iterable<? extends F> iterable;

    protected MappingIterable(@Nonnull Iterable<? extends F> iterable) {
        this.iterable = iterable;
    }

    @Override
    public Iterator<T> iterator() {
        return new MappingIterator(iterable.iterator());
    }

    public abstract T map(@Nullable F f);

    private final class MappingIterator implements Iterator<T> {
        private final Iterator<? extends F> iterator;
        private T next;

        private MappingIterator(Iterator<? extends F> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            while (next == null && iterator.hasNext()) {
                next = map(iterator.next());
            }
            return next != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T res = next;
            next = null;
            return res;
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    @Override
    public String toString() {
        return iterable.toString();
    }
}
