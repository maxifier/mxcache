/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage;

import gnu.trove.strategy.HashingStrategy;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Storage {
    HashingStrategy DEFAULT_HASHING_STRATEGY = new HashingStrategy() {
        @Override
        public int computeHashCode(Object object) {
            return object == null ? 0 : object.hashCode();
        }

        @Override
        public boolean equals(Object o1, Object o2) {
            return o1 == null ? o2 == null : o1.equals(o2);
        }
    };

    /** This object is returned by reference-value storages if no value is set for given key */
    Object UNDEFINED = new Undefined();

    void clear();

    int size();

    class Undefined implements Serializable {
        private static final long serialVersionUID = 0x1000L;

        @Override
        public String toString() {
            return "<UNDEFINED>";
        }

        @Override
        public int hashCode() {
            // large prime number
            return 0x13D4FD;
        }

        private Object readResolve() {
            return UNDEFINED;
        }
    }
}
