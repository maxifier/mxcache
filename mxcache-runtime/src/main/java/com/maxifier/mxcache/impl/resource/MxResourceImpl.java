/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.Serializable;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.ResourceModificationException;
import com.maxifier.mxcache.util.TIdentityHashSet;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class MxResourceImpl extends AbstractDependencyNode implements MxResource, Serializable {
    private static final Logger logger = LoggerFactory.getLogger(MxResourceImpl.class);

    private static final long serialVersionUID = 100L;

    protected final Object owner;
    @Nonnull
    private final String name;

    private final ReentrantReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    private final ResourceReadLock resourceReadLock;
    private final ResourceWriteLock resourceWriteLock;

    private TIdentityHashSet<ResourceViewable> oldDependentResourceViewNodes;

    public MxResourceImpl(Object owner, @Nonnull String name) {
        this.owner = owner;
        this.name = name;
        lock = new ReentrantReadWriteLock();

        readLock = lock.readLock();
        writeLock = lock.writeLock();

        resourceReadLock = new ResourceReadLock();
        resourceWriteLock = new ResourceWriteLock();
    }

    @Override
    public void readStart() throws ResourceModificationException {
        resourceReadLock.lock();
    }

    @Override
    public void readEnd() {
        resourceReadLock.unlock();
    }

    @Override
    public void writeStart() throws ResourceModificationException {
        resourceWriteLock.lock();
    }

    @Override
    public void writeEnd() {
        resourceWriteLock.unlock();
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isReading() {
        return lock.getReadHoldCount() > 0;
    }

    @Override
    public boolean isWriting() {
        return lock.isWriteLocked();
    }

    @Override
    public void waitForEndOfModification() {
        if (lock.isWriteLocked() && logger.isTraceEnabled()) {
            logger.trace("Thread {} is waiting for resource \"{}\"", Thread.currentThread(), this);
        }
        readLock.lock();
        try {
            if (lock.isWriteLockedByCurrentThread()) {
                throw new ResourceModificationException("Current thread (" + Thread.currentThread() + ") has already locked resource \"" + this + "\" for write");
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void clearDependentCaches() {
        DependencyTracker.deepInvalidate(this);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void addNode(@Nonnull CleaningNode cache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate() {
        // invalidate should not be invoked because resource is never added to DependencyTracker
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public Lock readLock() {
        return resourceReadLock;
    }

    @Nonnull
    @Override
    public Lock writeLock() {
        return resourceWriteLock;
    }

    private class ResourceReadLock implements Lock, Serializable {
        @Override
        public void lock() {
            DependencyNode node = DependencyTracker.get();
            if (node == null) {
                // no caches -> just lock
                readLock.lock();
            } else {
                lockFromCache(node);
            }
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            DependencyNode node = DependencyTracker.get();
            if (node == null) {
                // no caches -> just lock
                readLock.lockInterruptibly();
            } else {
                lockFromCache(node);
            }
        }

        @Override
        public boolean tryLock() {
            DependencyNode node = DependencyTracker.get();
            if (node == null) {
                // no caches -> just lock
                return readLock.tryLock();
            } else {
                lockFromCache(node);
                return true;
            }
        }

        @Override
        public boolean tryLock(long time, @Nonnull TimeUnit unit) throws InterruptedException {
            DependencyNode node = DependencyTracker.get();
            if (node == null) {
                // no caches -> just lock
                return readLock.tryLock(time, unit);
            } else {
                lockFromCache(node);
                return true;
            }
        }

        private void lockFromCache(DependencyNode node) {
            if (!readLock.tryLock()) {
                // it means that someone holds a write lock
                // notify caller caches about resource being locked
                // so they can release their locks in order to avoid deadlocks on cache cleaning.
                throw new ResourceOccupied(MxResourceImpl.this);
            }
            // tryLock will succeed if current thread holds a write lock, so check it
            if (lock.isWriteLockedByCurrentThread()) {
                // we have to release it!
                readLock.unlock();
                throw new ResourceModificationException("Resource \"" + name + "\" is already being written from current thread");
            }
            // we add dependency only if we can read the resource
            // if someone writes it at the moment there's no point in adding the dependency that would be cleaned
            // immediately
            if (!DependencyTracker.isDummyNode(node)) {
                //Dummy node means that dependency tracking is switched off
                trackDependency(node);
            }
        }

        @Override
        public void unlock() {
            readLock.unlock();
        }

        @Nonnull
        @Override
        public Condition newCondition() {
            return readLock.newCondition();
        }
    }

    private class ResourceWriteLock implements Lock, Serializable {
        @Override
        public void lock() {
            if (DependencyTracker.hasUnderlyingNode()) {
                throw new ResourceModificationException("Resource \"" + name + "\" modification is required while cache " + DependencyTracker.get() + " is found on the stack");
            }
            writeLock.lock();
            oldDependentResourceViewNodes = DependencyTracker.saveResourceViewNodes(MxResourceImpl.this);
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            if (DependencyTracker.hasUnderlyingNode()) {
                throw new ResourceModificationException("Resource \"" + name + "\" modification is required while cache " + DependencyTracker.get() + " is found on the stack");
            }
            writeLock.lockInterruptibly();
            oldDependentResourceViewNodes = DependencyTracker.saveResourceViewNodes(MxResourceImpl.this);
        }

        @Override
        public boolean tryLock() {
            if (DependencyTracker.hasUnderlyingNode()) {
                throw new ResourceModificationException("Resource \"" + name + "\" modification is required while cache " + DependencyTracker.get() + " is found on the stack");
            }
            if (!writeLock.tryLock()) {
                return false;
            }
            oldDependentResourceViewNodes = DependencyTracker.saveResourceViewNodes(MxResourceImpl.this);
            return true;
        }

        @Override
        public boolean tryLock(long time, @Nonnull TimeUnit unit) throws InterruptedException {
            if (DependencyTracker.hasUnderlyingNode()) {
                throw new ResourceModificationException("Resource \"" + name + "\" modification is required while cache " + DependencyTracker.get() + " is found on the stack");
            }
            if (!writeLock.tryLock(time, unit)) {
                return false;
            }
            oldDependentResourceViewNodes = DependencyTracker.saveResourceViewNodes(MxResourceImpl.this);
            return true;
        }

        @Override
        public void unlock() {
            if (!lock.isWriteLockedByCurrentThread()) {
                throw new IllegalStateException("clearDependentCachesInternal is invoked with write lock held");
            }
            try {
                DependencyTracker.deepInvalidateWithResourceView(MxResourceImpl.this);
                DependencyTracker.exitDependentResourceView(oldDependentResourceViewNodes);
                oldDependentResourceViewNodes = null;
            } finally {
                writeLock.unlock();
            }
        }

        @Nonnull
        @Override
        public Condition newCondition() {
            return writeLock.newCondition();
        }
    }
}
