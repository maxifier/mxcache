package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.transform.SmartReference;

import gnu.trove.*;
import java.util.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */

public class ObjectObjectWeakTroveStorage<K extends SmartReference, V> extends ObjectObjectTroveStorage<K, V> {
    private final List<K> removed = Collections.synchronizedList(new ArrayList<K>());

    public ObjectObjectWeakTroveStorage() {
    }

    public ObjectObjectWeakTroveStorage(TObjectHashingStrategy<K> strategy) {
        super(strategy);
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
