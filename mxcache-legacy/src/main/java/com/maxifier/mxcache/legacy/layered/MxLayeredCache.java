package com.maxifier.mxcache.legacy.layered;

import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.legacy.MxCacheProxy;
import com.maxifier.mxcache.legacy.MxStateHandler;
import com.maxifier.mxcache.legacy.MBeanRegistrator;
import com.maxifier.mxcache.legacy.MxCacheFlusher;
import com.maxifier.mxcache.legacy.converters.MxConvertHelper;
import com.maxifier.mxcache.proxy.*;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectIdentityHashingStrategy;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

public final class MxLayeredCache<T> extends MutableStatisticsImpl implements MxStateHandler, Statistics {
    private static final Logger logger = LoggerFactory.getLogger(MxLayeredCache.class);

    private static final int UPDATE_REUSAGE_PERIOD = 10;
    private static final int CLEAR_MAPS_PERIOD = 20;
    private static final int FLUSH_IO_STAT = 20;

    private final ReferenceQueue<MxLayeredStrategy<T>> queue = new ReferenceQueue<MxLayeredStrategy<T>>();
    private final ReferenceQueue<MxProxy> proxyQueue = new ReferenceQueue<MxProxy>();

    private final Set<Reference<MxLayeredStrategy<T>>> elements = new THashSet<Reference<MxLayeredStrategy<T>>>();
    private final Map<Reference<? extends MxProxy>, MxLayeredStrategy<T>> proxyCache = new THashMap<Reference<? extends MxProxy>, MxLayeredStrategy<T>>();
    private final MxList<MxLayeredStrategy<T>> shorttimeCache = new MxList<MxLayeredStrategy<T>>();

    private final Set<Map<?, ? extends MxCacheProxy<T>>> cacheMaps = new THashSet<Map<?, ? extends MxCacheProxy<T>>>(new TObjectIdentityHashingStrategy<Map<?, ? extends MxCacheProxy<T>>>());

    private int maxShorttimeLive;
    private int maxShorttimeSize;

    private final MxConvertHelper converter;

    private final MxReusageForecastManager<T> reusageForecastManager;

//    private final int normalShorttimeLive;
//    private final int normalShorttimeSize;
//    private float intencity;

    private final String name;

    @NotNull
    private final MxCacheLayer[] layers;

    @NotNull
    private final MxGenericProxyFactory<T, MxLayeredStrategy> proxyFactory;

    private int garbageCollected;

    private final int n;

    private int time;

    private long stateHandlerExecutionTime;

    //------------------------------------------------------------------------------------------------------------------

    private static void checkConverters(MxConvertHelper converter, MxCacheLayer[] layers) {
        for (int i = 1; i < layers.length; i++) {
            if (!converter.canConvert(i, 0)) {
                throw new IllegalStateException("Every layer should be convertable into the upper layer, but " + layers[i] + " is not");
            }
        }
    }

    public MxLayeredCache(MxLayeredCacheConfiguration<T> configuration,
                          MBeanRegistrator mBeanRegistrator,
                          MxLayeredCacheSerializator serializator,
                          MxCacheFlusher cacheFlusher) {
        this(configuration.getName(), configuration.getElementClass(), configuration.getCacheLayers(),
             configuration.getMaxShorttimeLive(), configuration.getMaxShorttimeSize(),
             configuration.getReusageForecastManager(),
             mBeanRegistrator, configuration.getConverter(), serializator,
             cacheFlusher);
    }

    private MxLayeredCache(
            String name,
            @NotNull Class<T> elementClass,
            @NotNull MxCacheLayer[] layers,
            int maxShorttimeLive,
            int maxShorttimeSize,
            @NotNull MxReusageForecastManager<T> reusageForecastManager,
            MBeanRegistrator mBeanRegistrator,
            MxConvertHelper converter,
            MxLayeredCacheSerializator serializator,
            MxCacheFlusher mxCacheFlusher) {
        checkConverters(converter, layers);
        this.reusageForecastManager = reusageForecastManager;
        this.converter = converter;

        n = layers.length;

        this.name = name;
        this.layers = layers;

        this.maxShorttimeLive = maxShorttimeLive;
        this.maxShorttimeSize = maxShorttimeSize;

//        this.normalShorttimeLive = maxShorttimeLive;
//        this.normalShorttimeSize = maxShorttimeSize;
//        intencity = 1f;
//        intencity = 1f;

        proxyFactory = MxProxyGenerator.getGenericProxyFactory(elementClass, MxLayeredStrategy.class);

        updateReusage();

        serializator.register(this);

        try {
            mBeanRegistrator.registerMBean(
                    String.format("service=MxCache,type=Layered,elem=%s", name.replaceAll("[^\\w\\d_]+", "_")),
                    new LayeredCacheControl(this));
        } catch (Exception e) {
            logger.error("Unable to register mbean", e);
        }

        mxCacheFlusher.registerHandler(this);
    }

    String getName() {
        return name;
    }

    //------------------------------------------------------------------------------------------------------------------

    public MxConvertHelper getConverter() {
        return converter;
    }

// todo debug it   
//    public synchronized void addIntencity(float newIntencity) {
//        intencity += newIntencity;
//
//        log.debug("*** MX CACHE {} INTENCITY IS NOW {}", this, intencity);
//
//        maxShorttimeSize = (int) (normalShorttimeSize * intencity);
//        maxShorttimeLive = (int) (normalShorttimeLive * intencity);
//
//        cleanUpShorttime();
//        for (MxCacheLayer layer : layers) {
//            layer.addIntencity(newIntencity);
//        }
//    }
//
//    private void cleanUpShorttime() {
//        while (shorttimeCache.getSize() > maxShorttimeSize) {
//            shorttimeCache.remove(shorttimeCache.getFirst());
//        }
//    }

    void registerStrategy(MxLayeredStrategy<T> strategy) {
        Reference<MxLayeredStrategy<T>> ref = new WeakReference<MxLayeredStrategy<T>>(strategy, queue);
        elements.add(ref);
        strategy.setSelfReference(ref);
    }

    /**
     * Внимание: прокси, возвращаемый этим методом будет реализовывать все интерфейсы-наследники T,
     * которые реализовывал исходный объект, однако прокси нельзя кастить к типу исходного объекта!!!
     *
     * @param value                  объект
     * @return кэширующий прокси
     */
    @SuppressWarnings({"unchecked"})
    public synchronized T createProxy(T value) {
        if (value instanceof MxProxy) {
            MxProxy<T, MxLayeredStrategy<T>> proxy = (MxProxy<T, MxLayeredStrategy<T>>) value;
            if (proxy.getValue().getManager() != this) {
                throw new MxCacheException("Proxy attached to " + proxy.getValue().getManager() + " could not be attached to " + this);
            }
            logger.warn("Proxy reattached: " + proxy);
            return value;
        }
        MxLayeredStrategy<T> strategy = new MxLayeredStrategy<T>(this, value, reusageForecastManager);
        T proxy = proxyFactory.createProxy((Class<T>) value.getClass(), strategy);
        WeakReference<MxProxy> proxyRef = new WeakReference<MxProxy>((MxProxy) proxy, proxyQueue);
        proxyCache.put(proxyRef, strategy);
        return proxy;
    }

    public synchronized long getStateHandlerExecutionTime() {
        return stateHandlerExecutionTime;
    }

    int getLayerCount() {
        return n;
    }

    MxCacheLayer getLayer(int index) {
        return layers[index];
    }

    @Override
    public synchronized void stateHandler() {
        long start = System.nanoTime();
        time++;

        processProxyReferences();

        processStrategyReferences();

        if (time % UPDATE_REUSAGE_PERIOD == 0) {
            updateReusage();
        }

        if (time % CLEAR_MAPS_PERIOD == 0) {
            clearCacheMaps();
        }

        if (time % FLUSH_IO_STAT == 0) {
            converter.flushStat();
        }

        int minTime = time - maxShorttimeLive;
        MxLayeredStrategy<T> first = shorttimeCache.getFirst();
        while (first != null && first.getLastQueryTime() < minTime) {
            first.clearShorttime();
            first = shorttimeCache.remove(first);
        }

        for (MxCacheLayer layer : layers) {
            layer.update(time);
        }
        long end = System.nanoTime();
        stateHandlerExecutionTime += (end-start);
    }

    private void processProxyReferences() {
        Reference<? extends MxProxy> proxyRef = proxyQueue.poll();
        while (proxyRef != null) {
            garbageCollected++;
            MxLayeredStrategy<T> element = proxyCache.remove(proxyRef);
            assert element != null : "Trying to remove already removed proxy";
            elements.remove(element.getSelfReference());
            // Мы можем безболезненно удалить эту стратению из кэшей, т.к. на неё гарантированно больше нет ссылок
            // (т.к. при создании прокси, ссылка никуда не отдается наружу, а записывается только в elements)
            element.removeFromAllCaches();
            proxyRef = proxyQueue.poll();
        }
    }

    private void processStrategyReferences() {
        Reference<? extends MxLayeredStrategy<T>> ref = queue.poll();
        while (ref != null) {
            garbageCollected++;
            //noinspection SuspiciousMethodCalls
            elements.remove(ref);
            ref = queue.poll();
        }
    }

    public int getTotalSize() {
        return proxyCache.size();
    }

    public synchronized int getShortTimeSize() {
        return shorttimeCache.getSize();
    }

    public synchronized String printKeyStatistics() {
        StringBuilder buf = new StringBuilder();
        buf.append("+++++++++++++++ MX CACHE STATE ").append(this).append("+++++++++++++++++++++++++++++\n");
        int misses = getMisses();
        if (misses != 0) {
            buf.append("*** CACHE MISSES: ").append(misses).append(" ***\n");
        }
        converter.showStat();
        Map<Object, Integer> someMap = new HashMap<Object, Integer>();
        for (Reference<MxLayeredStrategy<T>> element : elements) {
            MxLayeredStrategy<T> st = element.get();
            if (st != null) {
                Object key = st.getKey();
                Integer v = someMap.get(key);
                if (v == null) {
                    someMap.put(key, 1);
                } else {
                    someMap.put(key, v + 1);
                }
            }
        }
        if (!someMap.isEmpty()) {
            for (Map.Entry<Object, Integer> e : someMap.entrySet()) {
                buf.append(e.getKey()).append(": ").append(e.getValue()).append('\n');
            }
        }
        if (garbageCollected != 0) {
            buf.append("GARBAGE COLLECTED: ").append(garbageCollected).append('\n');
            garbageCollected = 0;
        }
        buf.append("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        return buf.toString();
    }

    private synchronized void updateReusage() {
        for (Reference<MxLayeredStrategy<T>> e : elements) {
            MxLayeredStrategy<T> strategy = e.get();
            if (strategy != null) {
                strategy.updateReusageForecast();
            }
        }
        for (MxCacheLayer layer : layers) {
            layer.reorderCache();
        }
    }

    void addAndUpdate(MxLayeredStrategy<T> e) {
        shorttimeCache.addToTail(e);
        while (shorttimeCache.getSize() > maxShorttimeSize) {
            MxLayeredStrategy<T> element = shorttimeCache.getFirst();
            element.clearShorttime();
            shorttimeCache.remove(element);
        }
    }

    void moveToEnd(MxLayeredStrategy<T> e) {
        shorttimeCache.remove(e);
        shorttimeCache.addToTail(e);
    }

    void removeFromShorttime(MxLayeredStrategy<T> e) {
        shorttimeCache.remove(e);
    }

    public synchronized <E, F extends MxCacheProxy<T>> Map<E, F> createElementToMap() {
        Map<E, F> map = Collections.synchronizedMap(new THashMap<E, F>());
        cacheMaps.add(map);
        return map;
    }

    public synchronized int getTime() {
        return time;
    }

    private synchronized void clearCacheMaps() {
        int count = 0;
        for (Map<?, ? extends MxCacheProxy<T>> cacheMap : cacheMaps) {
            for (Iterator<? extends Map.Entry<?, ? extends MxCacheProxy<T>>> it = cacheMap.entrySet().iterator(); it.hasNext();) {
                Map.Entry<?, ? extends MxCacheProxy<T>> entry = it.next();
                MxCacheProxy<T> value = entry.getValue();
                Resolvable<T> mxCacheElement = value.getElement();
                if (mxCacheElement instanceof MxLayeredStrategy) {
                    MxLayeredStrategy<T> strategy = (MxLayeredStrategy<T>) mxCacheElement;
                    if (!strategy.isConfident()) {
                        it.remove();
                        count++;
                    }
                }
            }
        }
        if (count > 0) {
            logger.debug("*** MX CACHE {} cleared {} elements from cache maps", this, count);
        }
    }

    public synchronized void clearCache() {
        MxLayeredStrategy<T> first = shorttimeCache.getFirst();
        while (first != null) {
            first.clearShorttime();
            first = shorttimeCache.remove(first);
        }
        for (MxCacheLayer layer : layers) {
            layer.clear();
        }
        for (Map<?, ? extends MxCacheProxy<T>> cacheMap : cacheMaps) {
            cacheMap.clear();
        }
        cacheMaps.clear();
    }

    //------------------------------------------------------------------------------------------------------------------

    @Override
    public String toString() {
        return name;
    }
}
