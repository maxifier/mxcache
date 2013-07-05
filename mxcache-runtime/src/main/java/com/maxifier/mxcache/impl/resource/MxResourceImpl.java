package com.maxifier.mxcache.impl.resource;

import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.Serializable;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.clean.CleaningHelper;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.ResourceModificationException;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 9:18:59
 */
class MxResourceImpl extends AbstractDependencyNode implements MxResource, Serializable, CleaningNode {
    private static final Logger logger = LoggerFactory.getLogger(MxResourceImpl.class);

    private static final long serialVersionUID = 100L;

    protected final Object owner;
    @NotNull
    private final String name;

    private final ReentrantReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    private TIdentityHashSet<CleaningNode> oldDependentResourceViewNodes;

    public MxResourceImpl(Object owner, @NotNull String name) {
        this.owner = owner;
        this.name = name;
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
    }

    @Override
    public void readStart() throws ResourceModificationException {
        DependencyNode node = DependencyTracker.get();
        if (node == null) {
            // если кэшей нет, то просто блокируем.
            readLock.lock();
        } else {
            if (!readLock.tryLock()) {
                // не получиться readLock может только если кто-то пишет
                // если к ресурсу обратился кэш, то мы должны ему сообщить об этом.
                throw new ResourceOccupied(this);
            }
            if (lock.isWriteLockedByCurrentThread()) {
                // если пишет текущий поток, то мы сможем получить readLock.
                // поэтому проверяем отдельно

                // надо обязательно освободить ресурс!
                readLock.unlock();
                throw new ResourceModificationException("Resource \"" + name + "\" is already being written from current thread");
            }
            // мы добавляем зависимость, только если мы можем прочитать ресурс.
            // если же его кто-то пишет, то нет необходимости добавлять зависимость, потому что прочитать мы ничего
            // пока не сможем, следовательно чистить пока нечего.
            if (!DependencyTracker.isDummyNode(node)) {
                //Dummy node означает, что отслеживание зависимостей не нужно
                trackDependency(node);
            }
        }
    }

    @Override
    public void readEnd() {
        readLock.unlock();
    }

    @Override
    public void writeStart() throws ResourceModificationException {
        if (DependencyTracker.hasUnderlyingNode()) {
            throw new ResourceModificationException("Resource \"" + name + "\" modification is required while cache " + DependencyTracker.get() + " is found on the stack");
        }
        writeLock.lock();
        oldDependentResourceViewNodes = DependencyTracker.saveResourceViewNodes(this);
    }

    @Override
    public void writeEnd() {
        clearDependentCachesInternal();
    }

    @NotNull
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

    private void clearDependentCachesInternal() {
        assert lock.isWriteLockedByCurrentThread();

        boolean readLockAcquired = false;
        TIdentityHashSet<CleaningNode> elementsAndDependent = null;
        try {
            try {
                elementsAndDependent = CleaningHelper.lockRecursive(this);
                readLock.lock();
                readLockAcquired = true;
            } finally {
                DependencyTracker.exitDependentResourceView(oldDependentResourceViewNodes);
                oldDependentResourceViewNodes = null;
                writeLock.unlock();
            }

            //elementsAndDependent will be changed during the invocation
            TIdentityHashSet<CleaningNode> changedDependentNodes = narrowDependenciesSet(elementsAndDependent);

            //clear changed nodes
            for (CleaningNode element : changedDependentNodes) {
                element.clear();
            }
        } finally {
            CleaningHelper.unlock(CleaningHelper.getLocks(elementsAndDependent));

            if (readLockAcquired) {
                readLock.unlock();
            }
        }
    }

    private TIdentityHashSet<CleaningNode> narrowDependenciesSet(TIdentityHashSet<CleaningNode> elementsAndDependent) {
        TIdentityHashSet<CleaningNode> changedDependentNodes = DependencyTracker.getChangedDependentNodes(this);

        //remove new nodes
        for (Iterator<CleaningNode> it = changedDependentNodes.iterator(); it.hasNext(); ) {
            CleaningNode node = it.next();
            if (!elementsAndDependent.contains(node)) {
                it.remove();
            }
        }

        //unlock not changed nodes
        for (Iterator<CleaningNode> it = elementsAndDependent.iterator(); it.hasNext(); ) {
            CleaningNode element = it.next();
            if (!changedDependentNodes.contains(element)) {
                Lock lock = element.getLock();
                if (lock != null) {
                    it.remove();
                    lock.unlock();
                }
            }
        }
        return changedDependentNodes;
    }

    @Override
    public void clearDependentCaches() {
        writeLock.lock();
        clearDependentCachesInternal();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
        // appendNodes не должне быть вызван, поскольку ресурс никогда не добавляется в DependencyTracker
        throw new UnsupportedOperationException();
    }

    @Override
    public void addNode(@NotNull CleaningNode cache) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Lock getLock() {
        return writeLock;
    }

    @Override
    public void clear() {
        // nothing to do
    }

    @Override
    public DependencyNode getDependencyNode() {
        return this;
    }

    @Override
    public Object getCacheOwner() {
        return owner;
    }
}
