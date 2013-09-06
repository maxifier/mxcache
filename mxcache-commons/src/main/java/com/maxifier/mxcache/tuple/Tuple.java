package com.maxifier.mxcache.tuple;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.02.2010
 * Time: 18:55:04
 *
 * <p>
 * Класс кортежа. Не стоит его реализовавыть вручную.
 * @see TupleGenerator
 */
public abstract class Tuple implements Serializable, Iterable<Object> {
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
     * Hash code of tuple should be equal to <code>java.util.Arrays.hashCode(tuple.toArray())</code>.
     * @return hash code of this tuple
     */
    @Override
    public abstract int hashCode();

    /**
     * Hash code of tuple with given hashing strategies
     * @param hashingStrategies array of trove hash strategies for each element; null means default behaveour
     * @return hashcode of tuple if there were special strategies
     */
    public abstract int hashCode(Object... hashingStrategies);

    /**
     * Tuples are equal of they have same type and corresponding elements are equal.
     *  (i.e. tuple with Wrapper differs from tuple with primitive even if their values are equal).
     * @param obj object
     * @return true if obj is tuple and this tuple is equal to obj.
     */
    @Override
    public abstract boolean equals(Object obj);

    /**
     * Tuples are equal of they have same type and corresponding elements are equal using corresponging hashing strategy.
     * (i.e. tuple with Wrapper differs from tuple with primitive even if their values are equal).
     *
     * <b>Custom strategies are available only for Object types! Primitive hashing strategies don't have equals!</b>
     *
     * @param obj object
     *
     * @param hashingStrategies стратегии хэширования; размер массива должен совпадать с размером кортежа; null означает
     * использование стратегии по-умолчанию
     * @return true if obj is tuple and this tuple is equal to obj.
     */
    public abstract boolean equals(Object obj, Object... hashingStrategies);

    @Override
    public abstract String toString();

    /**
     * @return array representation of this tuple.
     */
    @NotNull
    public Object[] toArray() {
        int size = size();
        Object[] array = new Object[size];
        for (int i = 0; i < size; i++) {
            array[i] = get(i);
        }
        return array;
    }

    @Override
    public Iterator<Object> iterator() {
        return new TupleIterator(this);
    }

}
