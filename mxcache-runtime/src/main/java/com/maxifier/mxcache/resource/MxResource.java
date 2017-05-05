/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.resource;

import javax.annotation.Nonnull;

import java.io.Serializable;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * <p>
 * MxResource is very similar to a usual read-write lock but it has an important feature:
 * it marks all caches that invoked its read lock dirty when corresponding write lock is released.
 * </p><p>
 * With MxResource you can organize automatic cleaning of cache that depends on certain data and also guard
 * yourself from dirty reads.
 * </p><p>
 * MxResource is guaranteed to prevent deadlocks on cache cleaning.
 * </p><p>
 * Use the usual lock access pattern  with try-finally to access resources:
 * <code>
 *     MxResource res = ...
 *     res.readStart();
 *     try {
 *         ...code goes here...
 *     } finally {
 *         res.readEnd();
 *     }
 * </code>
 * </p>
 *
 * @see com.maxifier.mxcache.impl.resource.MxResourceFactory
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface MxResource extends Serializable, ReadWriteLock {
    /**
     * @return resource name. Never changes.
     */
    @Nonnull
    String getName();

    /**
     * <p>
     * Corresponds to readLock().lock().
     * </p><p>
     * This method may throw {@link com.maxifier.mxcache.impl.resource.ResourceOccupied} error.
     * Please don't catch it as it may lead to deadlocks.
     *
     * @throws ResourceModificationException if current thread holds a write lock.
     * I.e. resource modification should not invoke caches that depend on the same resource.
     */
    void readStart() throws ResourceModificationException;

    /**
     * Corresponds to readLock().unlock();
     */
    void readEnd();

    /**
     * Corresponds to writeLock().lock()
     * @throws ResourceModificationException if there is a cached method in the stack of current thread.
     */
    void writeStart() throws ResourceModificationException;

    /**
     * Corresponds to writeLock().unlock().
     * This method causes cache cleaning immediately.
     * After it finishes it is guaranteed that no caches contain the dirty data that depend on previous state of
     * the resource.
     */
    void writeEnd();

    /**
     * Note: a state of isReading() can change quite frequently from other threads. Don't rely on the result unless
     * that's you who hold the lock.
     * @return true if someone holds read lock of the resource
     */
    boolean isReading();

    /**
     * Note: a state of isWriting() can change quite frequently from other threads. Don't rely on the result unless
     * that's you who hold the lock.
     * @return true if someone holds write lock of the resource
     */
    boolean isWriting();

    /**
     * Waits for other threads to release write lock.
     *
     * It doesn't guarantee that you can obtain the lock after it finishes as the lock may be obtained concurrently
     * by any other thread.
     * @throws ResourceModificationException if current thread holds write lock (prevent from hanging)
     */
    void waitForEndOfModification() throws ResourceModificationException;

    /**
     * Clears all dependent caches.
     * <b>Note: it internally obtains write lock.</b>
     */
    void clearDependentCaches();
}
