/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;
import javax.annotation.Nullable;

import java.util.*;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.resource.DependencyTracker;

/**
 * ClassCleanableInstanceList
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({"unchecked"})
final class ClassCleanableInstanceList<T> extends WeakList<T> {
    private final ClassCleanableInstanceList<? super T> parent;
    private final Class clazz;
    private final Cleanable<T> cleanable;
    private final Map<String, ClassCacheIds> groups;
    private final Map<String, ClassCacheIds> tags;

    //------------------------------------------------------------------------------------------------------------------

    public ClassCleanableInstanceList(@Nullable ClassCleanableInstanceList<? super T> parent, Cleanable<T> cleanable, Map<String, ClassCacheIds> groups, Map<String, ClassCacheIds> tags, Class clazz) {
        this.parent = parent;
        this.tags = tags;
        this.groups = groups;
        this.clazz = clazz;
        this.cleanable = cleanable;
    }

    //------------------------------------------------------------------------------------------------------------------

    public void clearCacheByGroup(Object o, String group) {
        T t = (T) o;
        DependencyTracker.deepInvalidate(getGroupInstanceCaches(group, t));
    }

    public void clearCacheByTag(Object o, String tag) {
        T t = (T) o;
        DependencyTracker.deepInvalidate(getTagInstanceCaches(tag, t));
    }

    private List<Cache> getTagInstanceCaches(String tag, Object t) {
        //noinspection CollectionWithoutInitialCapacity
        List<Cache> caches = new ArrayList<Cache>();
        ClassCleanableInstanceList<? super T> classList = this;
        while (classList != null) {
            classList.appendTagInstanceCaches(tag, t, caches);
            classList = classList.parent;
        }
        return caches;
    }

    private void appendTagInstanceCaches(String tag, Object t, List<Cache> caches) {
        if (tags != null) {
            ClassCacheIds ids = tags.get(tag);
            if (ids != null) {
                ids.appendInstanceCaches(cleanable, t, caches);
            }
        }
    }

    private List<Cache> getGroupInstanceCaches(String group, Object t) {
        //noinspection CollectionWithoutInitialCapacity
        List<Cache> caches = new ArrayList<Cache>();
        ClassCleanableInstanceList<? super T> classList = this;
        while (classList != null) {
            classList.appendGroupInstanceCaches(group, t, caches);
            classList = classList.parent;
        }
        return caches;
    }

    private void appendGroupInstanceCaches(String tag, Object t, List<Cache> caches) {
        if (groups != null) {
            ClassCacheIds ids = groups.get(tag);
            if (ids != null) {
                ids.appendInstanceCaches(cleanable, t, caches);
            }
        }
    }

    public void clearCache(Object o) {
        T t = (T) o;
        DependencyTracker.deepInvalidate(getInstanceCaches(t));
    }

    private List<CleaningNode> getInstanceCaches(T t) {
        //noinspection CollectionWithoutInitialCapacity
        List<CleaningNode> caches = new ArrayList<CleaningNode>();
        ClassCleanableInstanceList<? super T> classList = this;
        while (classList != null) {
            classList.cleanable.appendInstanceCachesTo(caches, t);
            classList = classList.parent;
        }
        return caches;
    }

    public Cleanable<T> getCleanable() {
        return cleanable;
    }

    @Override
    public String toString() {
        return "ClassCleanableInstanceList[" + clazz + "]"; 
    }
}
