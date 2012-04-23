package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 01.02.2010
 * Time: 16:00:13
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
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
        for (CustomClassCleanableInstanceList<?> customClassCleanableInstanceList : list) {
            customClassCleanableInstanceList.getCaches(caches);
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