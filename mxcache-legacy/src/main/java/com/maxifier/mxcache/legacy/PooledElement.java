package com.maxifier.mxcache.legacy;

import com.maxifier.mxcache.proxy.MxProxy;
import com.maxifier.mxcache.util.MultiLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.maxifier.mxcache.storage.Storage.UNDEFINED;


/**
 * Project: Smart Advertising
 * Created by: Yakoushin Andrey
 * Date: 02.02.2009
 * Time: 9:12:04
 * <p/>
 * Copyright (c) 1999-2008 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
class PooledElement<T> extends MultiLock.Sublock {
    static final boolean ALLOW_SAVE_TO_BYTES = false;

    private final ElementOwner<T> owner;

    private final PooledConverter<T> converter;

    private final Confidence confidence;

    private boolean inPool;

    private int lastUsePeriod;
    private int periods;
    private int calls;

    private T value;
    private byte[] bytes;
    private MxResource resource;

    private volatile double size;
    private float forecast;

    private long calculationStart;

    private void adjustSize(double change) {
        size += change;
        if (inPool) {
            owner.adjustSize(change);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public PooledElement(@NotNull ElementOwner<T> owner, @NotNull Confidence confidence, @NotNull PooledConverter<T> converter) {
        super(owner.getLock());
        this.confidence = confidence;
        this.owner = owner;
        this.converter = converter;
    }

    //------------------------------------------------------------------------------------------------------------------

    private void setBytes(@Nullable byte[] bytes) {
        assert owner.isHeldByCurrentThread();
        changed(this.bytes, bytes, MxPooledState.BYTES.getSize(), true);
        this.bytes = bytes;
    }

    private void setObject(@Nullable T value) {
        assert owner.isHeldByCurrentThread();
        changed(this.value, value, MxPooledState.READY.getSize(), true);
        this.value = value;
    }

    private void setResource(@Nullable MxResource resource) {
        assert owner.isHeldByCurrentThread();
        changed(this.resource, resource, MxPooledState.ONDISK.getSize(), false);
        this.resource = resource;
    }

    private void changed(Object oldValue, Object value, double size, boolean pooling) {
        if (value == null) {
            if (oldValue != null) {
                adjustSize(-size);
            }
        } else if (oldValue == null) {
            adjustSize(size);
            if (pooling) {
                addToPool();
            }
        }
    }

    void clear() {
        assert owner.isHeldByCurrentThread();
        setObject(null);
        setBytes(null);
        setResource(null);
    }

    public double getSize() {
        return size;
    }

    public void setInPool(boolean inPool) {
        assert owner.isHeldByCurrentThread();
        this.inPool = inPool;
    }

    //------------------------------------------------------------------------------------------------------------------

    public Object getValue() {
        if (!isHeldByCurrentThread()) {
            throw new IllegalStateException("Element should be locked before load");
        }
        owner.lock();
        try {
            updateCallsAndPeriod();
            if (value == null) {
                if (bytes == null && resource == null) {
                    calculationStart = System.nanoTime();
                    return UNDEFINED;
                }
            } else {
                owner.hit();
                return value;
            }
        } finally {
            owner.unlock();
        }
        long start = System.nanoTime();
        T newValue = bytes == null ? load(resource) : deserialize(bytes);
        long loadTime = System.nanoTime() - start;

        owner.lock();
        try {
            setObject(newValue);
            owner.miss(loadTime);
            owner.update();
            return newValue;
        } finally {
            owner.unlock();
        }
    }

    private void updateCallsAndPeriod() {
        assert owner.isHeldByCurrentThread();
        calls++;
        int period = owner.getPeriod();
        if (lastUsePeriod != period) {
            lastUsePeriod = period;
            periods++;
        }
    }

    public void setValue(T value) {
        setValue0(unproxy(value));
    }

    @SuppressWarnings({ "unchecked" })
    private T unproxy(T value) {
        if (value instanceof MxProxy) {
            return (T)((MxProxy)value).getValue().getValue();
        }
        return value;
    }

    private void setValue0(T value) {
        if (!isHeldByCurrentThread()) {
            throw new IllegalStateException("Element should be locked before load");
        }
        owner.lock();
        try {
            setObject(value);
            long time = System.nanoTime() - calculationStart;
            converter.reportCost(MxPooledState.PARAMETRIC, MxPooledState.READY, time);
            owner.miss(time);
            owner.update();
        } finally {
            owner.unlock();
        }
    }

    private void addToPool() {
        assert owner.isHeldByCurrentThread();
        if (!inPool) {
            owner.addToPool(this);
        }
    }

    /** Предсказывает (по крайней мере, пытается) число обращений к этому элементу. */
    void updateForecastCalls() {
        assert owner.isHeldByCurrentThread();
        forecast = PooledReusageForecast.getCallForecast(confidence, calls, periods, owner.getPeriod(), lastUsePeriod);
    }

    public float getForecast() {
        return forecast;
    }

    //------------------------------------------------------------------------------------------------------------------

    @NotNull
    private byte[] serialize(T value) {
        return converter.serialize(value);
    }

    @NotNull
    private T deserialize(byte[] bytes) {
        return converter.deserialize(bytes);
    }

    @NotNull
    private MxResource save(byte[] bytes, T value) {
        return converter.save(bytes, value);
    }

    @NotNull
    private T load(MxResource resource) {
        return converter.load(resource);
    }

    //------------------------------------------------------------------------------------------------------------------

    public final boolean isConfident() {
        return periods >= confidence.getPeriodsToBeConfident() && calls >= confidence.getCallsToBeConfident();
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * @return true if this element should be removed from pool
     */
    boolean compact() {
        assert owner.isHeldByCurrentThread();
        updateForecastCalls();
        if (value != null) {
            double bytesCost = bytesStorageCost();

            double distCost = diskStorageCost();

            double removeCost = removeCost();

            if (bytesCost < distCost) {
                if (bytesCost < removeCost) {
                    if (bytes == null) {
                        setBytes(serialize(value));
                    }
                } else if (bytes != null) {
                    setBytes(null);
                }
            }
            if (distCost < removeCost && resource == null) {
                setResource(save(bytes, value));
            }
            if (bytes != null) {
                setBytes(null);
            }
            setObject(null);
        }
        if (ALLOW_SAVE_TO_BYTES) {
            if (bytes != null) {
                double distCost = diskStorageCost();

                double removeCost = removeCost();

                if (distCost < removeCost && resource == null) {
                    setResource(save(bytes, value));
                }
                setBytes(null);
            }
        }
        return value == null && bytes == null;
    }

    private double removeCost() {
        return storageCost(MxPooledState.PARAMETRIC, false);
    }

    private double bytesStorageCost() {
        if (ALLOW_SAVE_TO_BYTES) {
            return Double.MAX_VALUE;
        }
        return storageCost(MxPooledState.BYTES, bytes == null);
    }

    private double diskStorageCost() {
        return storageCost(MxPooledState.ONDISK, resource == null);
    }

    private double storageCost(MxPooledState<?> state, boolean needsForward) {
        return converter.storageCost(state, needsForward, forecast);
    }

    public int getPeriods() {
        return periods;
    }

    public boolean isInPool() {
        assert owner.isHeldByCurrentThread();
        return inPool;
    }
}
