/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.NonInstrumentedCacheException;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.provider.CacheDescriptor;
import gnu.trove.map.hash.THashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 * CleanableRegister - this class maintains a list of Cleanable and cache instances.
 * It's a single access point to all cache cleaning operations.
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class CleanableRegister implements CacheCleaner {
    private static final Logger logger = LoggerFactory.getLogger(CleanableRegister.class);

    private final Map<Class<?>, ClassCleanableInstanceList<?>> classCleanMap = new THashMap<Class<?>, ClassCleanableInstanceList<?>>();
    private final Map<Class<?>, DependencyNode> classMapping = new THashMap<Class<?>, DependencyNode>();
    private final Map<String, DependencyNode> groupMapping = new THashMap<String, DependencyNode>();
    private final Map<String, DependencyNode> tagMapping = new THashMap<String, DependencyNode>();

    private static final Cleanable<?> EMPTY_CLEANABLE = new EmptyCleanable();

    //---- Methods called from static initializer of classes generated with MxCache instrumentator ---------------------

    /**
     * Called from generated class registration method
     * @param clazz registered class
     * @param cleanable cleaner for this class
     * @param groups cache id by group mapping
     * @param tags cache id by tag mapping
     * @param <T> type of class
     */
    public synchronized <T> void registerClass(Class<T> clazz, Cleanable<T> cleanable, @Nullable Map<String, ClassCacheIds> groups, @Nullable Map<String, ClassCacheIds> tags) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Interface could not have cached methods!");
        }
        ClassCleanableInstanceList<? super T> superClassList = getClassList(clazz.getSuperclass());

        //noinspection unchecked
        ClassCleanableInstanceList<T> instanceList = new ClassCleanableInstanceList<T>(superClassList, cleanable, groups, tags, clazz);
        ClassCleanableInstanceList<?> oldValue = classCleanMap.put(clazz, instanceList);
        if (oldValue != null) {
            throw new IllegalStateException(clazz + " already registered");
        }
    }

    @SuppressWarnings({ "unchecked" })
    private synchronized ClassCleanableInstanceList<Object> getClassList(Class cls) {
        if (cls == Object.class) {
            return null;
        }
        assert !cls.isInterface();
        ClassCleanableInstanceList list = classCleanMap.get(cls);
        if (list == null) {
            list = new ClassCleanableInstanceList(getClassList(cls.getSuperclass()), EMPTY_CLEANABLE, null, null, cls);
            classCleanMap.put(cls, list);
        }
        return list;
    }

    //---- Called from modified constructor of instrumented class ---------------------------------------

    public void registerInstance(Object o, Class<?> aClass) {
        ClassCleanableInstanceList<?> instanceList = getListByClass(aClass);
        if (instanceList == null) {
            throw new IllegalArgumentException(String.format("Object %s has no cache", o));
        }
        instanceList.add(o);
    }

    @SuppressWarnings({"unchecked"})
    public List<Cache> getCaches(@Nonnull CacheDescriptor descriptor) {
        ClassCleanableInstanceList<?> list = getListByClass(descriptor.getDeclaringClass());
        if (list == null) {
            logger.error("Unknown class required: " + descriptor.getDeclaringClass());
            return Collections.emptyList();
        }
        Cleanable cleanable = list.getCleanable();
        if (descriptor.isStatic()) {
            return Collections.singletonList(cleanable.getStaticCache(descriptor.getId()));
        }
        list.lock();
        try {
            if (list.isEmpty()) {
                return Collections.emptyList();
            }
            List<Cache> instances = new ArrayList<Cache>(list.size());
            for (Object instance : list) {
                instances.add(cleanable.getInstanceCache(instance, descriptor.getId()));
            }
            return instances;
        } finally {
            list.unlock();
        }
    }

    private synchronized ClassCleanableInstanceList<?> getListByClass(Class<?> aClass) {
        return classCleanMap.get(aClass);
    }

    //---- Called by used ----------------------------------------------------------------------------------------------


    @Override
    public void clearCacheByInstances(Object... o) {
        for (Object object : o) {
            clearCacheByInstance(object);
        }
    }

    @Override
    public void clearCacheByInstance(Object o) {
        Class<?> aClass = o.getClass();
        while (aClass != null && aClass != Object.class) {
            ClassCleanableInstanceList<?> cleanableInstanceList = getListByClass(aClass);
            if (cleanableInstanceList == null) {
                aClass = aClass.getSuperclass();
            } else {
                cleanableInstanceList.clearCache(o);
                return;
            }
        }
        checkNonInstrumentedCaches(o.getClass());
        throw new IllegalArgumentException(String.format("Object %s has no caches", o));
    }

    static void checkNonInstrumentedCaches(Class c) throws NonInstrumentedCacheException {
        while (c != null && c != Object.class) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getAnnotation(Cached.class) != null) {
                    throw new NonInstrumentedCacheException(m);
                }
            }
            c = c.getSuperclass();
        }
    }

    @Override
    public void clearInstanceByTag(Object o, String tag) {
        Class<?> aClass = o.getClass();
        while (aClass != null && aClass != Object.class) {
            ClassCleanableInstanceList<?> cleanableInstanceList = getListByClass(aClass);
            if (cleanableInstanceList == null) {
                aClass = aClass.getSuperclass();
            } else {
                cleanableInstanceList.clearCacheByTag(o, tag);
                return;
            }
        }
        checkNonInstrumentedCaches(o.getClass());
        throw new IllegalArgumentException(String.format("Object %s has no caches", o));
    }

    @Override
    public void clearInstanceByGroup(Object o, String group) {
        Class<?> aClass = o.getClass();
        while (aClass != null && aClass != Object.class) {
            ClassCleanableInstanceList<?> cleanableInstanceList = getListByClass(aClass);
            if (cleanableInstanceList == null) {
                aClass = aClass.getSuperclass();
            } else {
                cleanableInstanceList.clearCacheByGroup(o, group);
                return;
            }
        }
        checkNonInstrumentedCaches(o.getClass());
        throw new IllegalArgumentException(String.format("Object %s has no caches", o));
    }

    @Override
    public void clearCacheByClass(Class<?> clazz) {
        checkNonInstrumentedCaches(clazz);

        DependencyNode node;
        synchronized (this) {
            node = classMapping.get(clazz);
        }
        if (node == null) {
            logger.warn("There is no subclasses of " + clazz + " with caches yet");
        } else {
            DependencyTracker.deepInvalidate(node);
        }
    }

    @Override
    public void clearCacheByGroup(String group) {
        DependencyNode node;
        synchronized (this) {
            node = groupMapping.get(group);
        }
        if (node == null) {
            logger.warn("There is no caches of group <" + group + "> yet");
        } else {
            DependencyTracker.deepInvalidate(node);
        }
    }

    @Override
    public void clearCacheByTag(String tag) {
        DependencyNode node;
        synchronized (this) {
            node = tagMapping.get(tag);
        }
        if (node == null) {
            logger.warn("There is no caches with tag <" + tag + "> yet");
        } else {
            DependencyTracker.deepInvalidate(node);
        }
    }

    /**
     * This method is equivalent to cleaning a tag <code>@full.name.of.AnnotationClass</code>.
     */
    @Override
    public void clearCacheByAnnotation(Class<? extends Annotation> annotationClass) {
        DependencyNode node;
        synchronized (this) {
            node = tagMapping.get("@" + annotationClass.getName());
        }
        if (node == null) {
            logger.warn("Can`t find cache for annotation " + annotationClass);
        } else {
            DependencyTracker.deepInvalidate(node);
        }
    }

    @Nullable
    public synchronized DependencyNode getClassDependencyNode(Class<?> clazz) {
        DependencyNode tagNode = classMapping.get(clazz);
        if (tagNode == null) {
            tagNode = new EmptyDependencyNode("class:" + clazz.getName());
            classMapping.put(clazz, tagNode);
        }
        return tagNode;
    }

    public synchronized DependencyNode getTagDependencyNode(String tag) {
        DependencyNode tagNode = tagMapping.get(tag);
        if (tagNode == null) {
            tagNode = new EmptyDependencyNode("tag:" + tag);
            tagMapping.put(tag, tagNode);
        }
        return tagNode;
    }

    public synchronized DependencyNode getGroupDependencyNode(String group) {
        DependencyNode groupNode = groupMapping.get(group);
        if (groupNode == null) {
            groupNode = new EmptyDependencyNode("group:" + group);
            groupMapping.put(group, groupNode);
        }
        return groupNode;
    }

    private static class EmptyCleanable implements Cleanable<Object> {
        @Override
        public void appendInstanceCachesTo(List<CleaningNode> locks, Object o) {
            // interfaces has no caches
        }

        @Override
        public Cache getStaticCache(int id) {
            throw new IllegalArgumentException("Interface has no caches");
        }

        @Override
        public Cache getInstanceCache(Object o, int id) {
            throw new IllegalArgumentException("Interface has no caches");
        }
    }

    private static class EmptyDependencyNode extends AbstractDependencyNode {
        private final String name;

        public EmptyDependencyNode(String name) {
            this.name = name;
        }

        @Override
        public void invalidate() {
            // do nothing
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
