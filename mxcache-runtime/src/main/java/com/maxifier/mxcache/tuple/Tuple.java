/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.tuple;

import gnu.trove.strategy.HashingStrategy;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Tuple - don't try to implement it manually. All the specific implementations are generated on-the-flight by
 * TupleFactory and TupleGenerator. Tuples are immutable. Tuples are typed, i.e. it may hold only values of specific
 * type as it's fields. The types are defined at tuple creation time.
 *
 * @see TupleGenerator
 * @see TupleFactory
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class Tuple implements Serializable, Iterable<Object> {

    /**
     * Array of trove hash strategies for each element; null means default behaviour for element.
     * hashingStrategies.length == size().
     */
    protected final HashingStrategy[] hashingStrategies;

    protected Tuple(HashingStrategy[] hashingStrategies) {
        this.hashingStrategies = hashingStrategies;
    }

    /**
     * @param i element index
     * @return i-th element of tuple (wrapped if needed)
     */
    public abstract <T> T get(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not boolean
     */
    public abstract boolean getBoolean(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not byte
     */
    public abstract byte getByte(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not char or cannot be converted to char (i.e. is not also int, short, byte)
     */
    public abstract char getChar(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not short or cannot be converted to short (i.e. is not also char, byte)
     */
    public abstract short getShort(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     * @throws IllegalArgumentException if i-th element is not int or cannot be converted to int (i.e. is not also short, char, byte)
     */
    public abstract int getInt(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not long or cannot be converted to long (i.e. is not also int, short, char, byte)
     */
    public abstract long getLong(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not float
     */
    public abstract float getFloat(int i);

    /**
     * @param i element index
     *
     * @return i-th element of tuple
     *
     * @throws IllegalArgumentException if i-th element is not double or cannot be converted to double (i.e. is not also float)
     */
    public abstract double getDouble(int i);

    /**
     * @return size of this tuple (count of elements)
     */
    public abstract int size();

    /**
     * @return hash code of this tuple. Hash code computed using provided {@link #hashingStrategies}
     * or default hashCode for the i-th element type, if hashingStrategies[i] == null.
     */
    @Override
    public abstract int hashCode();

    /**
     * @param obj object
     * @return true if obj is tuple and this tuple is equal to obj. Elements equality computed
     * using provided {@link #hashingStrategies} or default equality for the i-th element type,
     * if hashingStrategies[i] == null.
     */
    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();

    @SuppressWarnings("unused")
    public HashingStrategy[] getHashingStrategies() {
        return hashingStrategies;
    }

    @Override
    public Iterator<Object> iterator() {
        return new TupleIterator(this);
    }

}
