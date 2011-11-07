package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.legacy.converters.*;
import com.maxifier.mxcache.util.FormatHelper;
import com.maxifier.mxcache.util.MultiLock;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.THashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;

/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 02.02.2009
 * Time: 9:06:42
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class MxCachePoolManager<T> extends ElementOwner<T> implements MxStateHandler {
    private static final Logger logger = LoggerFactory.getLogger(MxCachePoolManager.class);

    private static final double CLEAR_RATE = 0.8;
    private static final double RESIZE_CLEAR_RATE = 0.9;
    private static final long UNMASKED_CLEAR_TIME = 600000;
    private static final int MAX_YOUNG_SIZE_A = 3;
    private static final int MAX_YOUNG_SIZE_B = 10;
    private static final int MAX_YOUNG_PERIODS = 1;

    private final Set<PooledCache<?, T, ?>> caches = new TIdentityHashSet<PooledCache<?, T, ?>>();

    private double minRateToDecrease;
    private double limitIncreaseScaler;
    private double limitDecreaseScaler;

    private double minLimit;
    private boolean wasOverflow;

    private double optimalFree;
    private double freeVariation;
    private long minFreeMem;

    private double limit;
    private double minLimitToDecrease;

    private final String name;

    private final Queue<MxStageEvent> eventQueue = new ConcurrentLinkedQueue<MxStageEvent>();

    private long lastEventTime;
    private final IntervalStatistics periodStatistics;
    private final IntervalStatistics pauseStatistics;

    private long lastTimeCacheMapsClean;

    private final MemoryStat memoryStat = new MemoryStat();

    private int activePeriods;
    private int period;

    private double size;

    private long stateHandlerExecutionTime;

    private final PriorityQueue<PooledElement<T>> pool;
    private final List<PooledElement<T>> young;

    private final MxConvertHelper converter;

    private final Configuration configuration = new Configuration();

    private final MultiLock lock = new MultiLock();

    private final Map<MxConvertType, PooledConverter<T>> converters;

    //------------------------------------------------------------------------------------------------------------------

    public MxCachePoolManager(
            String name,
            double optimalFree,
            double freeVariation,
            long minFreeMem,
            MxConvertType[] types,
            MBeanRegistrator mbeanManager,
            MxResourceManager rm,
            MxCacheFlusher mxCacheFlusher,
            MxConverter<byte[], T> fromByteConverter,
            MxConverter<T, byte[]> toByteConverter,
            MxConverter<MxResource, T> fromResourceConverter,
            MxConverter<T, MxResource> toResourceConverter) {
        this(name, optimalFree, freeVariation, minFreeMem, types, mbeanManager, rm, mxCacheFlusher,
                fromByteConverter, toByteConverter, fromResourceConverter, toResourceConverter,
                null);
    }

    public MxCachePoolManager(
            String name,
            double optimalFree,
            double freeVariation,
            long minFreeMem,
            MxConvertType[] types,
            MBeanRegistrator mbeanManager,
            MxResourceManager rm,
            MxCacheFlusher mxCacheFlusher,
            MxConverter<byte[], T> fromByteConverter,
            MxConverter<T, byte[]> toByteConverter,
            MxConverter<MxResource, T> fromResourceConverter,
            MxConverter<T, MxResource> toResourceConverter,
            @Nullable String templateResourceFileName) {
        this.name = name;

        limitIncreaseScaler = 0.2;
        limitDecreaseScaler = -0.2;
        minRateToDecrease = 0.5;
        minLimit = 500;
        limit = minLimit;

        this.optimalFree = optimalFree;
        this.freeVariation = freeVariation;
        this.minFreeMem = minFreeMem;

        minLimitToDecrease = limit * minRateToDecrease;

        lastEventTime = System.currentTimeMillis();
        periodStatistics = new IntervalStatistics();
        pauseStatistics = new IntervalStatistics();

        lastTimeCacheMapsClean = System.currentTimeMillis();

        MxConvertHelperBuilder builder = new MxConvertHelperBuilder(rm, name, types,
                                                                    MxPooledState.READY,
                                                                    MxPooledState.PARAMETRIC,
                                                                    MxPooledState.BYTES,
                                                                    MxPooledState.ONDISK);

        builder.addConverter(MxPooledState.READY, MxPooledState.BYTES, toByteConverter, 30);
        builder.addConverter(MxPooledState.BYTES, MxPooledState.ONDISK, new MxConvertBytesToResource(rm, templateResourceFileName), 20);
        builder.addConverter(MxPooledState.READY, MxPooledState.ONDISK, toResourceConverter, 50);

        builder.addConverter(MxPooledState.BYTES, MxPooledState.READY, fromByteConverter, 1);
        builder.addConverter(MxPooledState.ONDISK, MxPooledState.BYTES, new MxConvertResourceToBytes(), 1);
        builder.addConverter(MxPooledState.ONDISK, MxPooledState.READY, fromResourceConverter, 2);

        builder.addConverter(MxPooledState.PARAMETRIC, MxPooledState.READY, new MxConvertResolvable<Object>(), 1);
        converter = builder.build();

        try {
            mbeanManager.registerMBean("service=MxCache,type=Pool,elem=" + name.replaceAll("[^\\w\\d_]+", "_"), new PooledCacheControl(this));
        } catch (Exception e) {
            logger.warn("Unable to register mbean", e);
        }

        pool = new PriorityQueue<PooledElement<T>>((int) (minLimit / 2), new PooledElementComparator<T>());
        young = new LinkedList<PooledElement<T>>();

        mxCacheFlusher.registerHandler(this);

        converters = new THashMap<MxConvertType, PooledConverter<T>>();
        for (MxConvertType type : types) {
            converters.put(type, new PooledConverterImpl<T>(type, converter));
        }
    }

    //************************* Настройна параметров *******************************************************************


    public Configuration getConfiguration() {
        return configuration;
    }

    public MxConvertHelper getConverter() {
        return converter;
    }

    @NotNull
    public PooledConverter<T> getConverter(MxConvertType t) {
        PooledConverter<T> c = converters.get(t);
        if (c == null) {
            throw new IllegalArgumentException("Unsupported type: " + t + ", but only " + converters.keySet() + " are supported");
        }
        return c;
    }

    public int getActivePeriods() {
        // There is no synchronization, so this method may return inaccurate value, but it is used only in JMX
        return activePeriods;
    }

    public double getMinLimitToDecrease() {
        // There is no synchronization, so this method may return inaccurate value, but it is used only in JMX
        return minLimitToDecrease;
    }

    public int getYoungCount() {
        lock();
        try {
            return young.size();
        } finally {
            unlock();
        }
    }

    public int getOldCount() {
        lock();
        try {
            return pool.size();
        } finally {
            unlock();
        }
    }

    public long getStateHandlerExecutionTime() {
        return stateHandlerExecutionTime;
    }

    /**
     * There is no synchronization on getters, so this method may return inaccurate value, but it is used only in JMX
     */
    public final class Configuration {
        public double getLimit() {
            return limit;
        }

        public double getLimitDecreaseScaler() {
            return limitDecreaseScaler;
        }

        public double getLimitIncreaseScaler() {
            return limitIncreaseScaler;
        }

        public double getMinRateToDecrease() {
            return minRateToDecrease;
        }

        public double getMinLimit() {
            return minLimit;
        }

        public long getMinFreeMem() {
            return minFreeMem;
        }

        public double getPoolSize() {
            return size;
        }

        public double getFreeVariation() {
            return freeVariation;
        }

        public double getOptimalFree() {
            return optimalFree;
        }

        public void setFreeVariation(double newFreeVariation) {
            lock();
            try {
                freeVariation = newFreeVariation;
            } finally {
                unlock();
            }
        }

        public void setOptimalFree(double newOptimalFree) {
            lock();
            try {
                optimalFree = newOptimalFree;
            } finally {
                unlock();
            }
        }

        public void setMinFreeMem(long newMinFreeMem) {
            lock();
            try {
                minFreeMem = newMinFreeMem;
            } finally {
                unlock();
            }
        }

        public void setMinLimit(double newMinLimit) {
            lock();
            try {
                minLimit = newMinLimit;
            } finally {
                unlock();
            }
        }

        public void setLimit(double newLimit) {
            lock();
            try {
                limit = newLimit;
            } finally {
                unlock();
            }
        }

        public void setLimitIncreaseScaler(double newLimitIncreaseScaler) {
            lock();
            try {
                limitIncreaseScaler = newLimitIncreaseScaler;
            } finally {
                unlock();
            }
        }

        public void setMinRateToDecrease(double newMinRateToDecrease) {
            lock();
            try {
                minRateToDecrease = newMinRateToDecrease;
            } finally {
                unlock();
            }
        }

        public void setLimitDecreaseScaler(double newLimitDecreaseScaler) {
            lock();
            try {
                limitDecreaseScaler = newLimitDecreaseScaler;
            } finally {
                unlock();
            }
        }

        public void clearTo(double rate) {
            lock();
            try {
                MxCachePoolManager.this.clearTo(rate);
            } finally {
                unlock();
            }
        }

        public int getPeriod() {
            lock();
            try {
                return MxCachePoolManager.this.getPeriod();
            } finally {
                unlock();
            }
        }
    }

    @Override
    int getPeriod() {
        assert isHeldByCurrentThread();
        return period;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    void unlock() {
        lock.unlock();
    }

    @Override
    void lock() {
        lock.lock();
    }

    @Override
    boolean isHeldByCurrentThread() {
        return lock.isHeldByCurrentThread();
    }

    //------------------------------------------------------------------------------------------------------------------

    @SuppressWarnings({ "unchecked" })
    private void updatePool() {
        assert isHeldByCurrentThread();
        PooledElement[] oldPoolContent = pool.toArray(new PooledElement[pool.size()]);
        pool.clear();
        for (PooledElement element : oldPoolContent) {
            assert element.isInPool();
            element.updateForecastCalls();
            pool.add(element);
        }
    }

    private void flushYoung() {
        assert isHeldByCurrentThread();
        for (PooledElement<T> element : young) {
            assert element.isInPool();
            element.updateForecastCalls();
            pool.add(element);
        }
        young.clear();
    }

    public int getTotalSize() {
        lock();
        try {
            int res = 0;
            for (PooledCache<?, T, ?> cache : caches) {
                res += cache.size();
            }
            return res;
        } finally {
            unlock();
        }
    }

    public void clearAll() {
        lock();
        try {
            for (PooledCache<?, T, ?> cache : caches) {
                cache.clear();
            }
        } finally {
            unlock();
        }
    }

    //Вызывается в потоке flusher`а, W не включён - включается внутри
    @Override
    public void stateHandler() {
        Lock wholeLock = lock.getWholeLock();
        wholeLock.lock();
        try {
            long start = System.nanoTime();
            boolean periodEnded = processEvents();
            updatePool();
            if (periodEnded) {
                flushYoung();
            }
            adjustSize();
            if (activePeriods == 0) {
                long currentTime = System.currentTimeMillis();
                double avg = pauseStatistics.getAverage();
                double mavg = Double.isNaN(avg) ? UNMASKED_CLEAR_TIME : (pauseStatistics.getMax() + avg) / 2;
                if (currentTime - lastTimeCacheMapsClean > mavg * 5) {
                    clearCacheMaps();
                    lastTimeCacheMapsClean = currentTime;
                }
            }
            long end = System.nanoTime();
            stateHandlerExecutionTime += end-start;
        } finally {
            wholeLock.unlock();
        }
    }

    /**
     * Вызывается в потоке flusher`а, W не включён - включается внутри
     *
     * @return true, если период закончился
     */
    private boolean processEvents() {
        assert isHeldByCurrentThread();
        boolean res = false;
        while (!eventQueue.isEmpty()) {
            MxStageEvent periodEvent = eventQueue.poll();
            if (periodEvent instanceof MxStageStartEvent) {
                if (activePeriods == 0) {
                    pauseStatistics.add(periodEvent.getTime() - lastEventTime);
                }
                period++;
                activePeriods++;
            } else {
                activePeriods--;
                periodStatistics.add(periodEvent.getTime() - lastEventTime);
                res = true;
            }
            lastEventTime = periodEvent.getTime();
        }
        return res;
    }

    //------------------------------------------------------------------------------------------------------------------

    // use only in PooledCache
    void registerCache(PooledCache<?, T, ?> cache) {
        lock();
        try {
            caches.add(cache);
        } finally {
            unlock();
        }
    }

    private void clearYoung() {
        clearElements(young);
    }

    private void clearElements(Collection<PooledElement<T>> collection) {
        assert isHeldByCurrentThread();
        for (PooledElement<T> element : collection) {
            element.clear();
            removing(element);
        }
        collection.clear();
    }

    //------------------------------------------------------------------------------------------------------------------

    // Проверяет переполнение кэша и в случае необходимости его очищает.

    @Override
    void update() {
        assert isHeldByCurrentThread();
        memoryStat.measure();

        if (limit < size) {
            wasOverflow = true;
            clearTo(CLEAR_RATE);
            if (limit < size) {
                megaClear();
            }
        }
    }

    private void adjustSize() {
        assert isHeldByCurrentThread();
        update();
        if (size < minLimitToDecrease && limit > minLimit) {
            if (logger.isTraceEnabled()) {
                logger.trace(String.format("*MxCachePoolManager Decrease cause too mush empty poolspace: %.2f %.2f", size, minLimitToDecrease));
            }
            decrease(1.0);
        }

        if (memoryStat.isAverageFreeLessThan(minFreeMem)) {
            if (limit > minLimit) {
                if (logger.isTraceEnabled()) {
                    logger.trace(String.format("*MxCachePoolManager Decrease cause min free memory limit exeeded: %sb < %sb", FormatHelper.formatSize(memoryStat.getAverageFree()), FormatHelper.formatSize(minFreeMem)));
                }
                decrease(1.0);
            }
        } else {
            double freeRate = memoryStat.getFreeRate();
            if (Math.abs(freeRate - optimalFree) > freeVariation) {
                if (freeRate < optimalFree) {
                    if (limit > minLimit) {
                        if (logger.isTraceEnabled()) {
                            logger.trace(String.format("*MxCachePoolManager Decrease cause there is not enough free memory: %.0f%% < %.0f%%", freeRate * 100, optimalFree * 100));
                        }
                        decrease(optimalFree - freeRate);
                    }
                } else if (wasOverflow) {
                    if (logger.isTraceEnabled()) {
                        logger.trace(String.format("*MxCachePoolManager Increase cause there was pool overflow and a lot of memory available: %.0f%% > %.0f%%", freeRate * 100, optimalFree * 100));
                    }
                    wasOverflow = false;
                    increase(freeRate - optimalFree);
                }
            }
        }

        memoryStat.reset();
    }

    private void decrease(double factor) {
        assert isHeldByCurrentThread();
        limit += limitDecreaseScaler * factor * limit;
        if (limit < minLimit) {
            limit = minLimit;
        }
        minLimitToDecrease = limit * minRateToDecrease;
        clearTo(RESIZE_CLEAR_RATE);
    }

    private void increase(double factor) {
        assert isHeldByCurrentThread();
        limit += limitIncreaseScaler * factor * limit;
        minLimitToDecrease = limit * minRateToDecrease;
    }

    @Override
    void addToPool(PooledElement<T> element) {
        assert isHeldByCurrentThread();
        adding(element);
        if (element.getPeriods() > MAX_YOUNG_PERIODS) {
            element.updateForecastCalls();
            pool.add(element);
        } else {
            young.add(element);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    private void removing(PooledElement<T> element) {
        assert isHeldByCurrentThread();
        assert element.isInPool();
        size -= element.getSize();
        element.setInPool(false);
    }

    private void adding(PooledElement<T> element) {
        assert isHeldByCurrentThread();
        assert !element.isInPool();
        size += element.getSize();
        element.setInPool(true);
    }

    //--- К Э Ш О О Ч И С Т И Т Е Л И    Д Л Я    О Б О И Х    П О Т О К О В -------------------------------------------

    private void clearTo(double rate) {
        assert isHeldByCurrentThread();
        long start = System.nanoTime();
        double oldSize = size;
        double wanted = limit * rate;
        if (oldSize > wanted) {
            int youngCount = young.size();
            int poolCount = pool.size();

            Iterator<PooledElement<T>> yit = young.iterator();
            Iterator<PooledElement<T>> pit = pool.iterator();

            while (size > wanted) {
                boolean youngReady = yit.hasNext();
                boolean poolReady = pit.hasNext();
                if (youngReady && (preferToClearYoung(youngCount, poolCount) || !poolReady)) {
                    PooledElement<T> e = yit.next();
                    if (e.compact()) {
                        yit.remove();
                        removing(e);
                        youngCount--;
                    }
                } else if (poolReady) {
                    PooledElement<T> e = pit.next();
                    if (e.compact()) {
                        pit.remove();
                        removing(e);
                        poolCount--;
                    }
                } else {
                    break;
                }
            }

            if (logger.isTraceEnabled()) {
                long end = System.nanoTime();
                if (end - start > 30000000) {
                    // если дольше 30 миллисекунд, то выведем сообщение
                    logger.trace(String.format("slow clear (from %.2f to %.2f / %.2f = %.2f cleared) in %d ms", oldSize, size, limit, (oldSize - size), (end - start) / 1000000));
                }
            }
        }
    }

    private static boolean preferToClearYoung(int youngCount, int poolCount) {
        return youngCount * MAX_YOUNG_SIZE_B > poolCount * MAX_YOUNG_SIZE_A;
    }

    private void megaClear() {
        assert isHeldByCurrentThread();
        clearYoung();
        clearTo(CLEAR_RATE);
    }

    public void removeFromPools(Collection<PooledElement<T>> elements) {
        assert isHeldByCurrentThread();
        flushYoung();
        for (PooledElement<T> element : elements) {
            if (pool.remove(element)) {
                removing(element);
            }
        }
    }

    private void clearCacheMaps() {
        converter.flushStat();
        int removed = 0;
        for (PooledCache<?, T, ?> cache : caches) {
            removed += cache.removeNonConfident();
        }
        if (removed > 0) {
            logger.debug("cleared " + removed + " elements from cache maps");
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%s [%.1f / %.1f]", name, size, limit);
    }

    //------------------------------------------------------------------------------------------------------------------

    public void periodStart(MxStageStartEvent event) {
        eventQueue.add(event);
    }

    public void periodFinish(MxStageEndEvent event) {
        eventQueue.add(event);
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    void adjustSize(double size) {
        assert isHeldByCurrentThread();
        this.size += size;
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    MultiLock getLock() {
        return lock;
    }

    private static class PooledElementComparator<T> implements Comparator<PooledElement<T>> {
        @Override
        public int compare(PooledElement<T> a, PooledElement<T> b) {
            return a == b ? 0 : Float.compare(a.getForecast(), b.getForecast());
        }
    }
}
