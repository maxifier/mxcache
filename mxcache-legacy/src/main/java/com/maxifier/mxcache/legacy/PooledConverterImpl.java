package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.legacy.converters.MxConvertHelper;
import com.maxifier.mxcache.legacy.converters.MxConvertType;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.02.11
 * Time: 17:14
 */
class PooledConverterImpl<T> implements PooledConverter<T> {
    private static final Logger logger = LoggerFactory.getLogger(PooledConverterImpl.class);

    private final MxConvertType type;
    private final MxConvertHelper converter;

    public PooledConverterImpl(MxConvertType type, MxConvertHelper converter) {
        this.type = type;
        this.converter = converter;
    }

    @Override
    @Nonnull
    public MxResource save(byte[] bytes, T value) {
        try {
            assert bytes != null || value != null : "Cannot save: both bytes and value are null";
            return bytes == null ?
                    converter.convert(MxPooledState.READY, MxPooledState.ONDISK, type, value) :
                    converter.convert(MxPooledState.BYTES, MxPooledState.ONDISK, type, bytes);
        } catch (Exception e) {
            logger.error("Cannot save", e);
            return null;
        }
    }

    @SuppressWarnings( { "unchecked" })
    @Override
    @Nonnull
    public T load(MxResource resource) {
        try {
            return (T) converter.convert(MxPooledState.ONDISK, MxPooledState.READY, type, resource);
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }

    @SuppressWarnings( { "unchecked" })
    @Override
    @Nonnull
    public T deserialize(byte[] bytes) {
        assert bytes != null;
        try {
            return (T) converter.convert(MxPooledState.BYTES, MxPooledState.READY, type, bytes);
        } catch (Exception e) {
            logger.error("Cannot deserialize", e);
            return null;
        }
    }

    @Override
    public byte[] serialize(T value) {
        assert value != null;
        try {
            return converter.convert(MxPooledState.READY, MxPooledState.BYTES, type, value);
        } catch (Exception e) {
            logger.error("Cannot serialize", e);
            return null;
        }
    }

    @Override
    public void reportCost(MxPooledState<?> from, MxPooledState<?> to, long time) {
        converter.reportCost(from, to, type, time);
    }

    @Override
    public double storageCost(MxPooledState<?> state, boolean needsForward, float usageForecast) {
        double cost = converter.getConvertCost(state, MxPooledState.READY, type) * usageForecast;
        if (needsForward) {
            cost += converter.getConvertCost(MxPooledState.READY, state, type);
        }
        cost /= state.getPreferrenceFactor();
        return cost;
    }
}
