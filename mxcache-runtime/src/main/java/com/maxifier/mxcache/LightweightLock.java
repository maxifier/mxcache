package com.maxifier.mxcache;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 23.11.11
 * Time: 15:45
 *
 * This lock is similar to {@link java.util.concurrent.locks.ReentrantLock} and is based on it's sync object.
 * But this lock requires much less memory than ReentrantLock.
 */
public class LightweightLock extends AbstractQueuedSynchronizer implements Lock {
    private static final long serialVersionUID = 1L;

    @Override
    protected boolean tryAcquire(int acquires) {
        Thread current = Thread.currentThread();
        int c = getState();
        if (c == 0) {
            if (compareAndSetState(0, acquires)) {
                setExclusiveOwnerThread(current);
                return true;
            }
        } else if (current == getExclusiveOwnerThread()) {
            c += acquires;
            if (c < 0) {
                throw new Error("Maximum lock count exceeded");
            }
            setState(c);
            return true;
        }
        return false;
    }

    @Override
    protected final boolean tryRelease(int releases) {
        int c = getState() - releases;
        if (Thread.currentThread() != getExclusiveOwnerThread()) {
            throw new IllegalMonitorStateException();
        }
        if (c == 0) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        } else {
            setState(c);
            return false;
        }
    }

    @Override
    protected final boolean isHeldExclusively() {
        return Thread.currentThread() == getExclusiveOwnerThread();
    }

    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        setState(0);
    }

    @Override
    public final ConditionObject newCondition() {
        return new ConditionObject();
    }

    @Override
    public final void lock() {
        if (compareAndSetState(0, 1)) {
            setExclusiveOwnerThread(Thread.currentThread());
        } else {
            acquire(1);
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return tryAcquire(1);
    }

    @Override
    public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException {
        return tryAcquireNanos(1, unit.toNanos(timeout));
    }

    @Override
    public void unlock() {
        release(1);
    }
}
