package com.maxifier.mxcache.legacy.layered;

import com.maxifier.mxcache.legacy.converters.MxConvertState;

import java.util.Iterator;
import java.util.PriorityQueue;

public final class MxCacheLayer<T> implements MxConvertState<T> {
    private final int id;

    private final String name;

//    private final int normalLongtimeSize;
//    private final int normalLongtimeLive;

    private int maxLongtimeSize;
    private int maxLongtimeLive;

//    private float intencity;

    private final float preferenceFactor;

    private final PriorityQueue<MxLayeredStrategy> longtimeCache = new PriorityQueue<MxLayeredStrategy>();

    private MxLayeredStrategy lastElementInLongtime;

    public float getPreferenceFactor() {
        return preferenceFactor;
    }

    public MxCacheLayer(int id, String name, int maxLongtimeSize, int maxLongtimeLive, float preferenceFactor) {
        this.id = id;
        this.name = name;
//        this.normalLongtimeSize = maxLongtimeSize;
//        this.normalLongtimeLive = maxLongtimeLive;
        this.maxLongtimeLive = maxLongtimeLive;
        this.maxLongtimeSize = maxLongtimeSize;
        this.preferenceFactor = preferenceFactor;

//        intencity = 1f;
    }

//    /**
//     * Должен вызываться только с синхронизацией на менеджера.
//     * @param newIntencity добавочная интенсивность
//     */
//    void addIntencity(float newIntencity) {
//        intencity += newIntencity;
//        maxLongtimeSize = (int)(normalLongtimeSize * intencity);
//        maxLongtimeLive = (int)(normalLongtimeLive * intencity);
//        cleanUp();
//    }

    /**
     * Should be called with synchronization on manager.
     */
    void reorderCache() {
        final MxLayeredStrategy[] objects = longtimeCache.toArray(new MxLayeredStrategy[longtimeCache.size()]);
        longtimeCache.clear();
        for (MxLayeredStrategy object : objects) {
            longtimeCache.offer(object);
        }
    }

    /**
     * Should be called with synchronization on manager.
     *
     * @param time time of manager
     */
    void update(int time) {
        int minTime = time - maxLongtimeLive;

        boolean removed = false;
        for (Iterator<MxLayeredStrategy> it = longtimeCache.iterator(); it.hasNext();) {
            MxLayeredStrategy cache = it.next();
            if (cache.getLastQueryTime() < minTime) {
                it.remove();
                cache.exitPool(id);
                removed = true;
            }
        }
        if (removed) {
            lastElementInLongtime = longtimeCache.peek();
        }
    }

    boolean canCache(MxLayeredStrategy element) {
        return longtimeCache.size() < maxLongtimeSize || lastElementInLongtime == null || lastElementInLongtime.compareTo(element) == -1;
    }

    boolean tryToCache(MxLayeredStrategy element) {
        if (longtimeCache.size() < maxLongtimeSize || lastElementInLongtime == null || lastElementInLongtime.compareTo(element) == -1) {
            longtimeCache.offer(element);
            cleanUp();
            return true;
        }
        return false;
    }

    void removeFromCache(MxLayeredStrategy element) {
        longtimeCache.remove(element);
    }

    private void cleanUp() {
        while (longtimeCache.size() > maxLongtimeSize) {
            longtimeCache.poll().exitPool(id);
        }
        lastElementInLongtime = longtimeCache.peek();
    }

    void clear() {
        while (!longtimeCache.isEmpty()) {
            longtimeCache.poll().exitPool(id);
        }
        lastElementInLongtime = null;
    }

    public String getName() {
        return name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
