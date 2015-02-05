/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.LightweightLock;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.locks.Lock;

/**
 * SuperLock is a lock that wraps over few usual locks and locks them simultaneously.
 * <p>
 * It implements a special locking algorithm that should prevents most common deadlocks, though it
 * may lead to starvation.
 * </p><p>
 * The algorithm is as follows:</p>
 * <ul>
 *     <li>go sequentially through the underlying locks;</li>
 *     <li>when end of array is reached, start from the beginning again;</li>
 *     <li>try to acquire current lock with tryLock();</li>
 *     <li>if lock is held by another thread:
 *     <ul>
 *         <li>release all previously acquired locks</li>
 *         <li>acquire current lock with lock();</li>
 *     </ul>
 *     <li>if not all locks are acquired yet go to start;</li>
 * </ul>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-31 11:17)
 */
@NotThreadSafe
public class SuperLock {
    private static final Logger logger = LoggerFactory.getLogger(SuperLock.class);

    /** masterLock is used to guard the whole SuperLock */
    private final LightweightLock masterLock;

    private final Lock[] locks;

    /**
     * Always holds the number of locked elements. Locks are rearranged in a way that only first <code>n</code>
     * elements from <code>locks</code> are locked.
     */
    private int n;

    public SuperLock(Collection<? extends Lock> locks) {
        this(locks.toArray(new Lock[locks.size()]));
    }

    public SuperLock(Lock[] locks) {
        this.locks = locks;

        masterLock = new LightweightLock();
    }

    protected void finalize() throws Throwable {
        // if there was some problems, notify a user about the problem
        if (n != 0) {
            logger.error("MxCache hasn't released {} locks: {}", n, Arrays.toString(locks));
        }
        super.finalize();
    }

    public void lock() {
        masterLock.lock();
        int i = 0;
        int firstLockedIndex = 0;
        int locked = 0;
        if (n != 0) {
            throw new IllegalMonitorStateException("SuperLock already locked");
        }
        boolean needsUnlock = true;
        try {
            while (locked < locks.length) {
                Lock lock = locks[i];
                if (!lock.tryLock()) {
                    for (; locked > 0; locked--) {
                        locks[firstLockedIndex++].unlock();
                        if (firstLockedIndex == locks.length) {
                            firstLockedIndex = 0;
                        }
                    }
                    lock.lock();
                }
                locked++;
                i++;
                if (i == locks.length) {
                    i = 0;
                }
            }
            n = locked;
            // clear the flag only if all locks were successfully acquired.
            // otherwise (e.g. if some locks thrown IllegalMonitorStateException or any other exception
            // we should unlock all the locks that were acquired so far

            // this is necessary because if SuperLock.lock() throws an exception nobody will call unlock on it
            needsUnlock = false;
        } finally {
            if (needsUnlock) {
                for (; locked > 0; locked--) {
                    locks[firstLockedIndex++].unlock();
                    if (firstLockedIndex == locks.length) {
                        firstLockedIndex = 0;
                    }
                }
            }
        }
    }

    public void unlock() {
        if (n > 0) {
            for (int i = 0; i < n; i++) {
                locks[i].unlock();
            }
            n = 0;
        }
        masterLock.unlock();
    }

    /**
     * This method allows to unlocks a part of locks.
     *
     * @throws IllegalMonitorStateException if this lock hasn't been previously locked with {@link #lock()}
     * @throws java.lang.IllegalArgumentException if some of locks passed don't belong to this lock or were already
     *      unlocked.
     */
    public void unlockPartially(TIdentityHashSet<Lock> locksToRelease) {
        if (!masterLock.isHeldByCurrentThread()) {
            throw new IllegalMonitorStateException("Lock must be held by current thread");
        }
        int to = 0;
        for (int i = 0; i<n; i++) {
            Lock lock = locks[i];
            if (locksToRelease.contains(lock)) {
                lock.unlock();
            } else {
                locks[to++] = lock;
            }
        }
        for (int i = to; i<n; i++) {
            locks[i] = null;
        }
        if (n - to != locksToRelease.size()) {
            throw new IllegalArgumentException("Unknown locks are passed to unlockPartially()");
        }
        n = to;
    }
}
