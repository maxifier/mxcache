/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CustomCleanableInstanceList
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
final class CustomCleanableInstanceList implements CleanableInstanceList {

    private final List<CustomClassCleanableInstanceList<?>> list;
    private final Lock readLock;
    private final Lock writeLock;

    private volatile int version;

    public CustomCleanableInstanceList() {
        //noinspection CollectionWithoutInitialCapacity
        list = new ArrayList<CustomClassCleanableInstanceList<?>>();
        ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        readLock = readWriteLock.readLock();
        writeLock = readWriteLock.writeLock();
    }

    //------------------------------------------------------------------------------------------------------------------

    public <T> void add(ClassCleanableInstanceList<T> classList, int[] classIds, int[] instanceIds) {
        writeLock.lock();
        try {
            version++;
            list.add(new CustomClassCleanableInstanceList<T>(classList, classIds, instanceIds));
        } finally {
            writeLock.unlock();
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public void clearCache() {
        CleaningHelper.clear(this);
    }

    @Override
    public void getCaches(List<CleaningNode> caches) {
        readLock.lock();
        try {
            for (CustomClassCleanableInstanceList<?> customClassCleanableInstanceList : list) {
                customClassCleanableInstanceList.getCaches(caches);
            }
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void deepUnlock() {
        readLock.unlock();
    }

    @Override
    public int deepLock() {
        readLock.lock();
        return version;
    }

    @Override
    public void getLists(List<WeakList<?>> lists) {
        for (CustomClassCleanableInstanceList<?> customClassCleanableInstanceList : list) {
            lists.add(customClassCleanableInstanceList.classList);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private static class CustomClassCleanableInstanceList<T> {
        private final ClassCleanableInstanceList<T> classList;
        private final int[] staticIds;
        private final int[] instanceIds;

        public CustomClassCleanableInstanceList(ClassCleanableInstanceList<T> classList, int[] staticIds, int[] instanceIds) {
            this.classList = classList;
            this.staticIds = staticIds;
            this.instanceIds = instanceIds;
        }

        public void getCaches(List<CleaningNode> caches) {
            Cleanable<T> cleanable = classList.getCleanable();
            for (int id : staticIds) {
                caches.add(cleanable.getStaticCache(id));
            }
            for (T t : classList) {
                for (int id : instanceIds) {
                    caches.add(cleanable.getInstanceCache(t, id));
                }
            }
        }
    }
}