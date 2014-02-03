package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.NonInstrumentedCacheException;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.provider.CacheDescriptor;
import gnu.trove.THashMap;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.lang.annotation.Annotation;

import com.maxifier.mxcache.caches.Cache;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 05.02.2010
 * Time: 9:47:32
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public final class CleanableRegister implements CacheCleaner {
    private static final Logger logger = LoggerFactory.getLogger(CleanableRegister.class);

    private final Map<Class<?>, ClassCleanableInstanceList<?>> classCleanMap = new THashMap<Class<?>, ClassCleanableInstanceList<?>>();
    private final Map<String, CustomCleanableInstanceList> groupCleanMap = new THashMap<String, CustomCleanableInstanceList>();
    private final Map<String, CustomCleanableInstanceList> tagCleanMap = new THashMap<String, CustomCleanableInstanceList>();

    private static final Cleanable<?> EMPTY_CLEANABLE = new EmptyCleanable();

    //---- Вызывается в секции статической инициализации инструментированного класса -----------------------------------

    /**
     * Метод вызывается в секции статической инициализации класса с кешем для регистрации.
     * @param clazz - регистрируемый класс
     * @param cleanable - чистильшик класса
     * @param groups - id кешей по группам
     * @param tags - id кешей по тегам
     * @param <T> - тип класса
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

        for (Class<?> intf : clazz.getInterfaces()) {
            getInterfaceList(intf).addChild(instanceList);
        }

        if (groups != null) {
            registerByString(instanceList, groupCleanMap, groups);
        }
        if (tags != null) {
            registerByString(instanceList, tagCleanMap, tags);
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

    @SuppressWarnings({ "unchecked" })
    private synchronized ClassCleanableInstanceList<Object> getInterfaceList(Class cls) {
        assert cls.isInterface();
        ClassCleanableInstanceList list = classCleanMap.get(cls);
        if (list == null) {
            list = new ClassCleanableInstanceList(null, EMPTY_CLEANABLE, null, null, cls);
            classCleanMap.put(cls, list);
        }
        return list;
    }

    private static <T> void registerByString(ClassCleanableInstanceList<T> instanceList, Map<String, CustomCleanableInstanceList> cacheMap, Map<String, ClassCacheIds> map) {
        for (Map.Entry<String, ClassCacheIds> entry : map.entrySet()) {
            ClassCacheIds value = entry.getValue();
            String key = entry.getKey();
            CustomCleanableInstanceList list = cacheMap.get(key);
            if (list == null) {
                list = new CustomCleanableInstanceList();
                cacheMap.put(key, list);
            }
            list.add(instanceList, value.getStaticIds(), value.getInstanceIds());
        }
    }

    //---- Вызывается в модифицированном конструкторе инструментированного класса---------------------------------------

    public void registerInstance(Object o, Class<?> aClass) {
        ClassCleanableInstanceList<?> instanceList = getListByClass(aClass);
        if (instanceList == null) {
            throw new IllegalArgumentException(String.format("Object %s has no cache", o));
        }
        instanceList.add(o);
    }

    @SuppressWarnings({"unchecked"})
    public List<Cache> getCaches(@Nonnull CacheDescriptor descriptor) {
        ClassCleanableInstanceList<?> list = getListByClass(descriptor.getOwnerClass());
        if (list == null) {
            logger.error("Unknown class required: " + descriptor.getOwnerClass());
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

    //---- Вызывется по запросу пользователя ---------------------------------------------------------------------------


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
    public void clearCacheByClass(Class<?> aClass) {
        ClassCleanableInstanceList<?> instanceList = getListByClass(aClass);
        if (instanceList == null) {
            checkNonInstrumentedCaches(aClass);
            // subclasses may be loaded later
            logger.warn("There is no subclasses of " + aClass + " with caches yet");
            return;
        }
        instanceList.clearCache();
    }

    @Override
    public void clearCacheByGroup(String group) {
        CustomCleanableInstanceList list = getListByGroup(group);
        if (list != null) {
            list.clearCache();
        } else {
            logger.warn("There is no caches of group <" + group + "> yet");
        }
    }

    private synchronized CustomCleanableInstanceList getListByGroup(String group) {
        return groupCleanMap.get(group);
    }

    @Override
    public void clearCacheByTag(String tag) {
        CustomCleanableInstanceList list = getListByTag(tag);
        if (list != null) {
            list.clearCache();
        } else {
            logger.warn("There is no caches with tag <" + tag + "> yet");
        }
    }

    private synchronized CustomCleanableInstanceList getListByTag(String tag) {
        return tagCleanMap.get(tag);
    }

    @Override
    public void clearAll(Collection<? extends CleaningNode> elements) {
        CleaningHelper.lockAndClear(elements);
    }

    /**
     * Из-за особенностей реализации, метод эквивалентен очистке по тегу <code>@полное.имя.класса.Аннотации</code>.
     */
    @Override
    public void clearCacheByAnnotation(Class<? extends Annotation> annotationClass) {
        CustomCleanableInstanceList list = getListByTag("@" + annotationClass.getName());
        if (list != null) {
            list.clearCache();
        } else {
            logger.warn("Can`t find cache for annotation " + annotationClass);
        }
    }

    private static class EmptyCleanable implements Cleanable<Object> {
        @Override
        public void appendStaticCachesTo(List<CleaningNode> locks) {
            // interfaces has no caches
        }

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
}
