package com.maxifier.mxcache.legacy.layered;

import com.maxifier.mxcache.legacy.MxResourceManager;
import com.maxifier.mxcache.legacy.converters.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.01.2009
 * Time: 14:44:00
 */
public final class MxLayeredCacheConfiguration<T> {
    private final String name;
    private final MxConvertType[] convertTypes;
    private final List<MxCacheLayer> layers;
    private MxConvertHelperBuilder converterBuilder;
    private MxCacheLayer[] cacheLayers;
    private MxReusageForecastManager<T> reusageForecastManager;
    private int maxShorttimeLive;
    private int maxShorttimeSize;

    @NotNull
    private final Class<T> elementClass;

    public MxLayeredCacheConfiguration(@NotNull String name, Class<T> elementClass, MxConvertType... convertTypes) {
        if (convertTypes == null || convertTypes.length == 0) {
            throw new IllegalArgumentException("No types specified!");
        }
        this.name = name;
        this.elementClass = elementClass;
        this.convertTypes = convertTypes;
        //noinspection CollectionWithoutInitialCapacity
        layers = new ArrayList<MxCacheLayer>();
    }

    public synchronized <T> MxCacheLayer<T> addLayer(@NotNull String name, int maxLongtimeSize, int maxLongtimeLive, float preferenceFactor) {
        if (converterBuilder != null) {
            throw new IllegalStateException("Cannot add layer after stable layer was added");
        }
        MxCacheLayer<T> layer = new MxCacheLayer<T>(layers.size(), name, maxLongtimeSize, maxLongtimeLive, preferenceFactor);
        layers.add(layer);
        return layer;
    }

    public synchronized <T> MxCacheLayer<T> addStableLayer(MxResourceManager rm, @NotNull String name, float preferenceFactor) {
        MxCacheLayer<T> layer = addLayer(name, 0, 0, preferenceFactor);

        cacheLayers = layers.toArray(new MxCacheLayer[layers.size()]);
        converterBuilder = new MxConvertHelperBuilder(rm, name, convertTypes, cacheLayers);

        return layer;
    }

    public synchronized <F, T> void addConverter(@NotNull MxConvertState<F> from, @NotNull MxCacheLayer<T> to, @NotNull MxConverter<F, T> converter, float avgLoadtime) {
        if (converterBuilder == null) {
            throw new IllegalStateException("Cannot add converter before all layers are added");
        }
        converterBuilder.addConverter(from, to, converter, avgLoadtime);
    }

    public void setMaxShorttimeLive(int maxShorttimeLive) {
        this.maxShorttimeLive = maxShorttimeLive;
    }

    public void setMaxShorttimeSize(int maxShorttimeSize) {
        this.maxShorttimeSize = maxShorttimeSize;
    }

    public String getName() {
        return name;
    }

    @NotNull
    public Class<T> getElementClass() {
        return elementClass;
    }

    synchronized MxCacheLayer[] getCacheLayers() {
        return cacheLayers;
    }

    public int getMaxShorttimeLive() {
        return maxShorttimeLive;
    }

    public int getMaxShorttimeSize() {
        return maxShorttimeSize;
    }

    public MxReusageForecastManager<T> getReusageForecastManager() {
        return reusageForecastManager;
    }

    public void setReusageForecastManager(MxReusageForecastManager<T> reusageForecastManager) {
        this.reusageForecastManager = reusageForecastManager;
    }

    synchronized MxConvertHelper getConverter() {
        if (converterBuilder == null) {
            throw new IllegalStateException("Invalid configuration: no stable layer was added");
        }
        return converterBuilder.build();
    }
}