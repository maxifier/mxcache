/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage;

import java.io.Serializable;

/**
 * Storage is the parent class for all storage classes. Don't implement this class directly, instead implement one of
 * its distinct subclasses like "ObjectStorage", "IntObjectStorage", etc.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Storage {

    /** This object is returned by reference-value storages if no value is set for given key */
    Object UNDEFINED = new Undefined();

    /**
     * Should immediately clear the storage. After this invocation no outdated data should be available.
     * May be blocking.
     */
    void clear();

    /** @return approximate number of stored elements */
    int size();

    /**
     * This is marker-class, it represents the value missing in cache.
     * Once the consumer receives this object it should know that there's no record in cache for given key.
     *
     *
     * This class should be singleton, the only instance is stored in {@link Storage#UNDEFINED}. Don't use
     * instanceof checks for this class. Just compare the object with Storage.UNDEFINED instead.
     *
     * Feel free to serialize this class. It will be deserialized to exactly the same instance.
     *
     * Don't move this class to anywhere because this will break serialization (class name is stored inside of
     * serialized classes).
     *
     */
    class Undefined implements Serializable {
        private static final long serialVersionUID = 0x1000L;

        Undefined() {
            // package-local constructor to prevent others from instantiating it.
            // use singleton instance Storage.UNDEFINED
        }

        @Override
        public String toString() {
            return "<UNDEFINED>";
        }

        @Override
        public int hashCode() {
            // large prime number
            return 0x13D4FD;
        }

        // Undefined should be singleton. Deserialization will return the same instance.
        private Object readResolve() {
            return Storage.UNDEFINED;
        }
    }
}
