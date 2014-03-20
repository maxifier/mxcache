/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import javax.annotation.Nonnull;

import java.util.Iterator;

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

    public abstract T map(F f);

    private final class MappingIterator implements Iterator<T> {
        private final Iterator<? extends F> iterator;

        private MappingIterator(Iterator<? extends F> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public T next() {
            return map(iterator.next());
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
