package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.locks.Lock;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 12:02:33
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public final class CleaningHelper {

    private CleaningHelper() {
    }

    public static void lock(List<Lock> locks) {
        int i = 0;
        int size = locks.size();
        int firstLockedIndex = 0;
        int locked = 0;
        while (locked < size) {
            Lock lock = locks.get(i);
            if (!lock.tryLock()) {
                for (; locked > 0; locked--) {
                    locks.get(firstLockedIndex++).unlock();
                    if (firstLockedIndex == size) {
                        firstLockedIndex = 0;
                    }
                }
                lock.lock();
            }
            locked++;
            i++;
            if (i == size) {
                i = 0;
            }
        }
    }

    public static void unlock(List<Lock> locks) {
        for (Lock lock : locks) {
            lock.unlock();
        }
    }

    public static List<Lock> getLocks(Collection<? extends CleaningNode> caches) {
        List<Lock> locks = new ArrayList<Lock>(caches.size());
        for (CleaningNode cache : caches) {
            Lock lock = cache.getLock();
            if (lock != null) {
                locks.add(lock);
            }
        }
        return locks;
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

    public static void clear(@NotNull CleanableInstanceList list) {
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
            // цикл проверки модификации зависимостей
            while (true) {
                List<Lock> locks = getLocks(elementsAndDependent);
                lock(locks);
                try {
                    TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
                    if (!newElements.equals(elementsAndDependent)) {
                        // набор зависимых кэшей изменился, придется еще раз все блокировать заново
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
                    unlock(locks);
                }
            }
        }
    }

    public static TIdentityHashSet<CleaningNode> lockRecursive(DependencyNode initial) {
        TIdentityHashSet<CleaningNode> elements = DependencyTracker.getAllDependentNodes(Collections.singleton(initial));
        TIdentityHashSet<CleaningNode> elementsAndDependent = elements;
        Iterable<DependencyNode> nodes = nodeMapping(elements);
        while (true) {
            List<Lock> locks = getLocks(elementsAndDependent);
            lock(locks);
            TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
            if (!newElements.equals(elementsAndDependent)) {
                // набор зависимых кэшей изменился, придется еще раз все блокировать заново
                elementsAndDependent = newElements;
                continue;
            }
            return elementsAndDependent;
        }
    }

    public static void lockAndClear(Collection<? extends CleaningNode> elements) {
        Iterable<DependencyNode> nodes = nodeMapping(elements);
        TIdentityHashSet<CleaningNode> elementsAndDependent = DependencyTracker.getAllDependentNodes(nodes, elements);
        while (true) {
            List<Lock> locks = getLocks(elementsAndDependent);
            lock(locks);
            try {
                TIdentityHashSet<CleaningNode> newElements = DependencyTracker.getAllDependentNodes(nodes, elements);
                if (!newElements.equals(elementsAndDependent)) {
                    // набор зависимых кэшей изменился, придется еще раз все блокировать заново
                    elementsAndDependent = newElements;
                    continue;
                }
                for (CleaningNode element : elementsAndDependent) {
                    element.clear();
                }
                return;
            } finally {
                unlock(locks);
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
}
