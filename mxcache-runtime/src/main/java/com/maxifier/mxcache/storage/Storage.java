package com.maxifier.mxcache.storage;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.09.2010
 * Time: 9:01:15
 */
public interface Storage {
    /** This object is returned by reference-value storages if no value is set for given key */
    Object UNDEFINED = new Serializable() {
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
    };

    void clear();

    int size();
}
