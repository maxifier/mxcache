package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.util.MultiLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.11.2010
 * Time: 14:57:42
 */
public abstract class ElementOwner<T> extends MutableStatisticsImpl {
    abstract int getPeriod();

    abstract void addToPool(PooledElement<T> s);

    abstract void adjustSize(double size);

    abstract void lock();

    abstract void unlock();

    abstract boolean isHeldByCurrentThread();

    abstract MultiLock getLock();

    abstract void update();
}
