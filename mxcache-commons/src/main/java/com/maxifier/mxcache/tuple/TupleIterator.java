package com.maxifier.mxcache.tuple;

import java.util.Iterator;

/**
* Created by IntelliJ IDEA.
* User: kochurov
* Date: 26.01.12
* Time: 16:06
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
        return objects.get(index++);
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
