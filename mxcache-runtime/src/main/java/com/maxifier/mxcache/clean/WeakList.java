/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.LightweightLock;
import com.maxifier.mxcache.transform.SmartReference;
import com.maxifier.mxcache.transform.SmartReferenceManager;

import javax.annotation.Nonnull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.ref.WeakReference;

/**
 * WeakList
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class WeakList<T> implements Iterable<T> {
    private final LightweightLock lock;

    private volatile InstanceReference head;
    private volatile int version;
    private volatile int size;

    public WeakList() {
        lock = new LightweightLock();
    }

    /**
     * @param ref reference to remove
     * @return reference to next element
     */
    private InstanceReference remove(InstanceReference ref) {
        lock.lock();
        try {
            size--;
            InstanceReference prev = ref.prev;
            InstanceReference next = ref.next;
            if (prev == null) {
                head = next;
                if (next != null) {
                    next.prev = null;
                }
            } else {
                prev.next = next;
                if (next != null) {
                    next.prev = prev;
                }
            }
            return next;
        } finally {
            lock.unlock();
        }
    }

    public void add(@Nonnull Object t) {
        lock.lock();
        try {
            version++;
            size++;
            //noinspection unchecked
            InstanceReference ref = new InstanceReference((T) t);
            ref.next = head;
            if (head != null) {
                head.prev = ref;
            }
            head = ref;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Locks the list.
     * @return collection version (the number of elements added from the beginning)
     */
    public int lock() {
        lock.lock();
        return version;
    }

    public void unlock() {
        lock.unlock();
    }

    @Override
    public Iterator<T> iterator() {
        assert lock.isHeldByCurrentThread();
        return new InstanceIterator();
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private class InstanceReference extends WeakReference<T> implements SmartReference, Runnable {
        private volatile InstanceReference next;
        private volatile InstanceReference prev;

        InstanceReference(T referent) {
            super(referent, SmartReferenceManager.getReferenceQueue());
        }

        @Override
        public Runnable getCallback() {
            return this;
        }

        @Override
        public void setCallback(Runnable callback) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void run() {
            remove(this);
        }
    }

    private class InstanceIterator implements Iterator<T> {
        private InstanceReference nextReference = head;
        private T instance;

        @Override
        public boolean hasNext() {
            while (nextReference != null && (instance = nextReference.get()) == null) {
                nextReference = WeakList.this.remove(nextReference);
            }
            return nextReference != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T res = instance;
            nextReference = nextReference.next;
            instance = null;
            return res;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
