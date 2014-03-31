/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.util.TIdentityHashSet;

import javax.annotation.Nonnull;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * CleaningHelper
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CleaningHelper {

    private CleaningHelper() {
    }

    private static SuperLock getSuperLock(Collection<? extends CleaningNode> caches) {
        List<Lock> locks = new ArrayList<Lock>(caches.size());
        for (CleaningNode cache : caches) {
            Lock lock = cache.getLock();
            if (lock != null) {
                locks.add(lock);
            }
        }
        return new SuperLock(locks);
    }

    private static int lockLists(List<WeakList<?>> lists) {
        int version = 0;
        for (WeakList<?> list : lists) {
            version += list.lock();
        }
        return version;
    }

    private static void unlockLists(List<WeakList<?>> lists) {
        for (WeakList<?> list : lists) {
            list.unlock();
        }
    }

    private static void clear(Collection<? extends CleaningNode> caches) {
        for (CleaningNode cache : caches) {
            cache.clear();
        }
    }

    public static void clear(@Nonnull CleanableInstanceList list) {
        List<WeakList<?>> lists = new ArrayList<WeakList<?>>();
        List<CleaningNode> elements = new ArrayList<CleaningNode>();

        Iterable<DependencyNode> nodes = nodeMapping(elements);

        outer: while (true) {
            int subtreeVersion = list.deepLock();
            try {
                lists.clear();
                list.getLists(lists);
            } finally {
                list.deepUnlock();
            }

            int listsVersion = lockLists(lists);
            try {
                elements.clear();
                list.getCaches(elements);
            } finally {
                unlockLists(lists);
            }
            TIdentityHashSet<CleaningNode> elementsAndDependent = DependencyTracker.getAllDependentNodes(nodes, elements);
            // dependency modification check loop
            while (true) {
                SuperLock superLock = getSuperLock(elementsAndDependent);
                superLock.lock();
                try {
                    TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
                    if (!newElements.equals(elementsAndDependent)) {
                        // the set of dependent caches has been altered, lock everything again
                        elementsAndDependent = newElements;
                        continue;
                    }
                    int newSubtreeVersion = list.deepLock();
                    try {
                        if (newSubtreeVersion != subtreeVersion) {
                            continue outer;
                        }
                        int newListsVersion = lockLists(lists);
                        try {
                            if (newListsVersion != listsVersion) {
                                continue outer;
                            }
                            clear(elementsAndDependent);
                            return;
                        } finally {
                            unlockLists(lists);
                        }
                    } finally {
                        list.deepUnlock();
                    }
                } finally {
                    superLock.unlock();
                }
            }
        }
    }

    /**
     * Collects all dependent nodes of root node and locks them.
     * @param rootNode root node to lookup dependencies from
     * @return a list of dependent caches and a super lock for this list.
     *    Note: returned super lock is <b>already locked</b> unless there was an exception thrown.
     */
    public static RecursiveLock lockRecursive(DependencyNode rootNode) {
        TIdentityHashSet<CleaningNode> elements = DependencyTracker.getAllDependentNodes(Collections.singleton(rootNode));
        Iterable<DependencyNode> nodes = nodeMapping(elements);
        while (true) {
            SuperLock superLock = getSuperLock(elements);
            superLock.lock();
            try {
                TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
                if (!newElements.containsAll(elements)) {
                    // we have to unlock all locks and lock them again among with new ones as advanced locking algorithm
                    // locks guarantees the absence of deadlocks only if all locks are locked at once.
                    superLock.unlock();
                    elements.addAll(newElements);
                    continue;
                }
                return new RecursiveLock(elements, superLock);
            } catch (Error e) {
                // we have to unlock it on exception
                superLock.unlock();
                throw e;
            } catch (RuntimeException e) {
                // we have to unlock it on exception
                superLock.unlock();
                throw e;
            }
        }
    }

    public static void lockAndClear(Collection<? extends CleaningNode> elements) {
        Iterable<DependencyNode> nodes = nodeMapping(elements);
        TIdentityHashSet<CleaningNode> elementsAndDependent = DependencyTracker.getAllDependentNodes(nodes, elements);
        while (true) {
            SuperLock superLock = getSuperLock(elementsAndDependent);
            superLock.lock();
            try {
                TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
                if (newElements.equals(elementsAndDependent)) {
                    for (CleaningNode element : elementsAndDependent) {
                        element.clear();
                    }
                    return;
                }
                // the set of dependent caches has been altered, lock everything again
                elementsAndDependent = newElements;
            } finally {
                superLock.unlock();
            }
        }
    }

    private static Iterable<DependencyNode> nodeMapping(final Collection<? extends CleaningNode> elements) {
        return new MappingIterable<CleaningNode, DependencyNode>(elements) {
            @Override
            public DependencyNode map(CleaningNode cleaningNode) {
                return cleaningNode.getDependencyNode();
            }
        };
    }

    public static class RecursiveLock {
        public final TIdentityHashSet<CleaningNode> elements;
        public final SuperLock lock;

        public RecursiveLock(TIdentityHashSet<CleaningNode> elements, SuperLock lock) {
            this.elements = elements;
            this.lock = lock;
        }
    }
}
