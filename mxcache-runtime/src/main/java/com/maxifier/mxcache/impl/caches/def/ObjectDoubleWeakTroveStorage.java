/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.transform.SmartReference;

import java.util.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM O2PWeakTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectDoubleWeakTroveStorage<T extends SmartReference> extends ObjectDoubleTroveStorage<T> {
    private final List<T> removed = Collections.synchronizedList(new ArrayList<T>());

    public ObjectDoubleWeakTroveStorage() {
    }

    public ObjectDoubleWeakTroveStorage(gnu.trove.strategy.HashingStrategy<T> strategy) {
        super(strategy);
    }

    private void cleanup() {
        for(T t: removed) {
            remove(t);
        }
        removed.clear();
    }

    @Override
    public double load(T o) {
        cleanup();
        return super.load(o);
    }

    @Override
    public void save(final T o, double t) {
        cleanup();
        o.setCallback(new Callback(o));
        super.save(o, t);
    }

    private class Callback implements Runnable {
        private final T o;

        public Callback(T o) {
            this.o = o;
        }

        @Override
        public void run() {
            removed.add(o);
        }
    }
}
