/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * TupleIterator
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TupleIterator implements Iterator<Object> {
    private final Tuple objects;
    private int index;

    public TupleIterator(Tuple objects) {
        this.objects = objects;
        this.index = 0;
    }

    @Override
    public boolean hasNext() {
        return index < objects.size();
    }

    @Override
    public Object next() {
        if (index >= objects.size()) {
            throw new NoSuchElementException();
        }
        return objects.get(index++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
