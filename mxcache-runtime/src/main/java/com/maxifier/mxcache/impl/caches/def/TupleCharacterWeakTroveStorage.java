/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.tuple.Tuple;
import com.maxifier.mxcache.transform.SmartReference;
import gnu.trove.strategy.HashingStrategy;

import java.util.*;

/**
 * TupleCharacterWeakTroveStorage
 *
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM O2PTupleWeakTroveStorage.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class TupleCharacterWeakTroveStorage extends ObjectCharacterTroveStorage<Tuple> {
    private final List<Tuple> removed = Collections.synchronizedList(new ArrayList<Tuple>());

    private final int[] tupleIndices;

    public TupleCharacterWeakTroveStorage() {
        throw new UnsupportedOperationException("TupleCharacterWeakTroveStorage actually is not externalizable");
    }

    public TupleCharacterWeakTroveStorage(int[] tupleIndices) {
        this.tupleIndices = tupleIndices;
    }

    public TupleCharacterWeakTroveStorage(HashingStrategy<Tuple> strategy, int[] tupleIndices) {
        super(strategy);
        this.tupleIndices = tupleIndices;
    }

    private void cleanup() {
        for(Tuple t: removed) {
            remove(t);
        }
        removed.clear();
    }

    @Override
    public char load(Tuple o) {
        cleanup();
        return super.load(o);
    }

    @Override
    public void save(Tuple o, char t) {
        cleanup();
        Callback callback = new Callback(o);
        for (int index: tupleIndices) {
            ((SmartReference)o.get(index)).setCallback(callback);
        }
        super.save(o, t);
    }

    private class Callback implements Runnable {
        private final Tuple o;

        public Callback(Tuple o) {
            this.o = o;
        }

        @Override
        public void run() {
            removed.add(o);
        }
    }
}
