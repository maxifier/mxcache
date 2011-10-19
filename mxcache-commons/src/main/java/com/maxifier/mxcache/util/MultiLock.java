package com.maxifier.mxcache.util;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.11.2010
 * Time: 13:21:33
 */
public class MultiLock {
    private final ReentrantLock lock = new ReentrantLock();

    private final Condition condition = lock.newCondition();

    private volatile int sublocked;

    public void lock() {
        lock.lock();
    }

    public void unlock() {
        lock.unlock();
    }

    public static class Sublock {
        private final MultiLock multilock;

        private volatile Thread owner;

        private volatile int depth;

        public Sublock(MultiLock multilock) {
            this.multilock = multilock;
        }

        public void lock() {
            multilock.lock.lock();
            try {
                Thread currentThread = Thread.currentThread();
                while (owner != null && owner != currentThread) {
                    multilock.condition.awaitUninterruptibly();
                }
                multilock.sublocked++;
                depth++;
                owner = currentThread;
            } finally {
                multilock.lock.unlock();
            }
        }

        public void unlock() {
            multilock.lock.lock();
            try {
                if (owner != Thread.currentThread()) {
                    throw new IllegalMonitorStateException("This lock wasn't locked by thread " + Thread.currentThread());
                }
                depth--;
                if (depth == 0) {
                    owner = null;
                }
                multilock.sublocked--;
                multilock.condition.signalAll();
            } finally {
                multilock.lock.unlock();
            }
        }

        public boolean isHeldByCurrentThread() {
            // если мы владеем локом, то никто не сможет изменить ссылку без нас; а если не владеем, то
            // все равно текущий поток не сможет завладеть локом пока не покинет этот метод
            return owner == Thread.currentThread();
        }

        public boolean isLocked() {
            return owner != null;
        }
    }

    public boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    private final Lock wholeLock = new AllLock();

    public Lock getWholeLock() {
        return wholeLock;
    }

    private class AllLock implements Lock {
        @Override
        public void lock() {
            lock.lock();
            while (sublocked != 0) {
                condition.awaitUninterruptibly();
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            lock.lockInterruptibly();
            while (sublocked != 0) {
                condition.await();
            }
        }

        @Override
        public boolean tryLock() {
            if (lock.tryLock()) {
                if (sublocked == 0) {
                    return true;
                }
                lock.unlock();
            }
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            throw new UnsupportedOperationException("MultiLock doesn't support timed try lock");
        }

        @Override
        public void unlock() {
            lock.unlock();
        }

        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException("MultiLock doesn't support conditions");
        }
    }
}
