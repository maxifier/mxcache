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

public class ObjectShortWeakTroveStorage<T extends SmartReference> extends ObjectShortTroveStorage<T> {
    private final List<T> removed = Collections.synchronizedList(new ArrayList<T>());

    public ObjectShortWeakTroveStorage() {
    }

    public ObjectShortWeakTroveStorage(TObjectHashingStrategy<T> strategy) {
        super(strategy);
    }

    private void cleanup() {
        for(T t: removed) {
            remove(t);
        }
        removed.clear();
    }

    @Override
    public short load(T o) {
        cleanup();
        return super.load(o);
    }

    @Override
    public void save(final T o, short t) {
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
