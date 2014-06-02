/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.transform.SmartReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ObjectObjectWeakTroveStorage
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ObjectObjectWeakTroveStorage<K extends SmartReference, V> extends ObjectObjectTroveStorage<K, V> {
    private final List<K> removed = Collections.synchronizedList(new ArrayList<K>());

    public ObjectObjectWeakTroveStorage() {
    }

    private void cleanup() {
        for(K k : removed) {
            remove(k);
        }
        removed.clear();
    }

    @Override
    public Object load(K o) {
        cleanup();
        return super.load(o);
    }

    @Override
    public void save(final K o, V t) {
        cleanup();
        o.setCallback(new Callback(o));
        super.save(o, t);
    }

    private class Callback implements Runnable {
        private final K o;

        public Callback(K o) {
            this.o = o;
        }

        @Override
        public void run() {
            removed.add(o);
        }
    }
}
