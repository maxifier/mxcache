/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class Sequence {
    private AtomicInteger position = new AtomicInteger();
    private AtomicReference<Throwable> exception = new AtomicReference<Throwable>();

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public synchronized void order(int order) throws Throwable {
        while (exception.get() == null && !position.compareAndSet(order, order+1)) {
            if (position.get() > order) {
                exception.set(new Exception("Expected " + order));
            }
            try {
                wait();
            } catch (InterruptedException e) {
                exception.set(e);
            }
        }
        check();
        notifyAll();
    }

    public void check() throws Throwable {
        Throwable e = exception.get();
        if (e != null) {
            throw e;
        }
    }

    public void fail(Throwable e) {
        exception.set(e);
        synchronized (this) {
            notifyAll();
        }
    }
}
