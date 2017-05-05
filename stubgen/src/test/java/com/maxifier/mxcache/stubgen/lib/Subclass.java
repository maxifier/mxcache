/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Subclass
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 16:47)
 */
public class Subclass extends Parent implements OtherInterface<String>, Iterable<String> {
    @Override
    public void x() {

    }

    @Override
    public void someAbstractMethod() {
    }

    @Override
    public void run() {
    }

    @Override
    public void w(String s) {

    }

    @Override
    public EmptyIterator iterator() {
        return new EmptyIterator();
    }

    public static class EmptyIterator implements Iterator<String> {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public String next() {
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
