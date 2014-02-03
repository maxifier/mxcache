package com.maxifier.mxcache.legacy.layered;

import com.maxifier.mxcache.legacy.MBeanRegistrator;
import com.maxifier.mxcache.legacy.MxCacheFlusher;
import com.maxifier.mxcache.legacy.MxResourceManager;
import com.maxifier.mxcache.legacy.converters.MxConvertState;
import com.maxifier.mxcache.legacy.converters.MxConvertType;
import com.maxifier.mxcache.legacy.converters.MxConverter;

import javax.annotation.Nonnull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 29.01.2009
 * Time: 14:44:00
 * <p>
 * Используйте конфигурацию (MxLayeredCacheConfiguration) вместо этого класса.
 * @see MxLayeredCacheConfiguration
 * @see MxLayeredCache#MxLayeredCache(MxLayeredCacheConfiguration, com.maxifier.mxcache.legacy.MBeanRegistrator , MxLayeredCacheSerializator, MxCacheFlusher)
 */
@Deprecated
public final class MxLayeredCacheBuilder<T> {
    private final MxLayeredCacheConfiguration<T> configuration;

    public MxLayeredCacheBuilder(@Nonnull String name, Class<T> elementClass, MxConvertType... convertTypes) {
        configuration = new MxLayeredCacheConfiguration<T>(name, elementClass, convertTypes);
    }

    public <T> MxCacheLayer<T> addLayer(@Nonnull String name, int maxLongtimeSize, int maxLongtimeLive, float preferenceFactor) {
        return configuration.addLayer(name, maxLongtimeSize, maxLongtimeLive, preferenceFactor);
    }

    public <T> MxCacheLayer<T> addStableLayer(MxResourceManager rm, @Nonnull String name, float preferenceFactor) {
        return configuration.addStableLayer(rm, name, preferenceFactor);
    }

    public <F, T> void addConverter(@Nonnull MxConvertState<F> from, @Nonnull MxCacheLayer<T> to, @Nonnull MxConverter<F, T> converter, float avgLoadtime) {
        configuration.addConverter(from, to, converter, avgLoadtime);
    }

    public synchronized MxLayeredCache<T> build(
            MxReusageForecastManager<T> reusageForecastManager,
            int maxShorttimeLive,
            int maxShorttimeSize,
            MBeanRegistrator mBeanRegistrator,
            MxLayeredCacheSerializator serializator,
            MxCacheFlusher mxCacheFlusher) {
        configuration.setReusageForecastManager(reusageForecastManager);
        configuration.setMaxShorttimeLive(maxShorttimeLive);
        configuration.setMaxShorttimeSize(maxShorttimeSize);
        return new MxLayeredCache<T>(configuration, mBeanRegistrator, serializator, mxCacheFlusher);
    }
}
