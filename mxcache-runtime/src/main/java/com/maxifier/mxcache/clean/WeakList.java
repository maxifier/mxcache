package com.maxifier.mxcache.clean;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.lang.ref.WeakReference;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 01.02.2010
 * Time: 15:36:35
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
class WeakList<T> implements Iterable<WeakReference<T>> {
    private final List<WeakReference<T>> internalList;

    private final Lock lock;

    private volatile int version;

    public WeakList() {
        //noinspection CollectionWithoutInitialCapacity
        internalList = new LinkedList<WeakReference<T>>();
        lock = new ReentrantLock();
    }

    public boolean add(Object t) {
        lock.lock();
        try {
            version++;
            //noinspection unchecked
            return internalList.add(new WeakReference<T>((T) t));
        } finally {
            lock.unlock();
        }
    }

    public int lock() {
        lock.lock();
        return version;
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public Iterator<WeakReference<T>> iterator() {
        return internalList.iterator();
    }

    public int size() {
        return internalList.size();
    }

    public boolean isEmpty() {
        return internalList.isEmpty();
    }
}
