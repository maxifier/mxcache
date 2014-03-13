/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.transform.SmartReference;

import gnu.trove.*;
import java.util.*;

/**
 * ObjectCharacterWeakTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectCharacterWeakTroveStorage<T extends SmartReference> extends ObjectCharacterTroveStorage<T> {
    private final List<T> removed = Collections.synchronizedList(new ArrayList<T>());

    public ObjectCharacterWeakTroveStorage() {
    }

    public ObjectCharacterWeakTroveStorage(TObjectHashingStrategy<T> strategy) {
        super(strategy);
    }

    private void cleanup() {
        for(T t: removed) {
            remove(t);
        }
        removed.clear();
    }

    @Override
    public char load(T o) {
        cleanup();
        return super.load(o);
    }

    @Override
    public void save(final T o, char t) {
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
