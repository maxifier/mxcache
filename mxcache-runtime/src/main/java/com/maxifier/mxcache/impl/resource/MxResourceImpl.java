package com.maxifier.mxcache.impl.resource;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.io.Serializable;
import java.io.ObjectStreamException;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.CleaningNode;
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
final class MxResourceImpl extends AbstractDependencyNode implements MxResource, Serializable, CleaningNode {
    private static final Logger logger = LoggerFactory.getLogger(MxResourceImpl.class);

    private static final long serialVersionUID = 100L;

    @NotNull
    private final String name;

    private final ReentrantReadWriteLock lock;
    private final Lock readLock;
    private final Lock writeLock;

    public MxResourceImpl(@NotNull String name) {
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
            if (node != DependencyTracker.DUMMY_NODE) {
                // DUMMY_NODE означает, что отслеживание зависимостей не нужно
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
    }

    @Override
    public void writeEnd() {
        clearDependentCaches();
        writeLock.unlock();
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

    @Override
    public void clearDependentCaches() {
        Set<CleaningNode> elements = DependencyTracker.getAllDependentNodes(Collections.<DependencyNode>singleton(this));
        CacheFactory.getCleaner().clearAll(elements);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MxResourceImpl that = (MxResourceImpl) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MxResourceSerializableImpl(this);
    }

    @Override
    public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
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
}
