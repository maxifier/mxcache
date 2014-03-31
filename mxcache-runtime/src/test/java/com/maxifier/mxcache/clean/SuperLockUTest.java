/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.util.TIdentityHashSet;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.testng.Assert.*;

/**
 * SuperLockUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-31 11:32)
 */
@Test
public class SuperLockUTest {
    public void testAlreadyLocked() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            SuperLock superLock = new SuperLock(Arrays.asList(lock));
            superLock.lock();
            superLock.unlock();
        } finally {
            lock.unlock();
        }
    }

    public void testBatchLock() throws InterruptedException {
        final List<Lock> locks = Arrays.<Lock>asList(new ReentrantLock(), new ReentrantLock(), new ReentrantLock());
        final SuperLock lock = new SuperLock(locks);

        class WaiterThread extends Thread {
            boolean locked;

            @Override
            public void run() {
                lock.lock();
                lock.unlock();
                locked = true;
            }
        }

        locks.get(0).lock(); // locked: 0

        WaiterThread t = new WaiterThread();
        t.setDaemon(true);
        t.start();

        locks.get(1).lock(); // locked: 0, 1

        t.join(10);

        locks.get(2).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(0).unlock(); // locked: 1, 2

        t.join(10);

        locks.get(0).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(1).unlock(); // locked: 0, 2

        t.join(10);

        locks.get(0).unlock(); // locked: 2

        t.join(10);

        locks.get(0).lock(); // locked: 0, 2

        t.join(10);

        locks.get(1).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(2).unlock(); // locked: 0, 1

        t.join(10);

        locks.get(1).unlock(); // locked: 0

        t.join(10);
        assertFalse(t.locked);

        locks.get(0).unlock(); // nothing locked

        t.join();
        assertTrue(t.locked);
    }

    public void testPartialUnlock() {
        final List<ReentrantLock> locks = Arrays.asList(new ReentrantLock(), new ReentrantLock(), new ReentrantLock());
        final SuperLock superLock = new SuperLock(locks);
        superLock.lock();
        try {
            for (ReentrantLock lock : locks) {
                assertTrue(lock.isHeldByCurrentThread());
            }
            TIdentityHashSet<Lock> toUnlock = new TIdentityHashSet<Lock>(Arrays.asList(locks.get(0)));
            superLock.unlockPartially(toUnlock);
            assertFalse(locks.get(0).isLocked());
            assertTrue(locks.get(1).isHeldByCurrentThread());
            assertTrue(locks.get(2).isHeldByCurrentThread());
        } finally {
            superLock.unlock();
        }
        for (ReentrantLock lock : locks) {
            assertFalse(lock.isLocked());
        }
    }
}
