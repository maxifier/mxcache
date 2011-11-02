package com.maxifier.mxcache.legacy.layered;

import com.magenta.dataserializator.MxObjectInput;
import com.magenta.dataserializator.MxObjectOutput;
import com.maxifier.mxcache.MxCacheException;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import com.maxifier.mxcache.proxy.Resolvable;
import com.maxifier.mxcache.proxy.MxProxy;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.ref.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"SynchronizeOnNonFinalField", "SynchronizationOnLocalVariableOrMethodParameter"})
public final class MxLayeredStrategy<T> extends MxList.Element<MxLayeredStrategy<T>> implements Externalizable, Comparable<MxLayeredStrategy<T>>, Resolvable<T> {
    private static final Logger logger = LoggerFactory.getLogger(MxLayeredStrategy.class);

    private static final long serialVersionUID = -5171623695018264162L;

    private static final float MIN_REUSAGE_TO_PRESERVE = 5.0f;

    private MxLayeredCache<T> manager;

    /**
     * —сылка на себ€: используетс€ дл€ удалени€ себ€ из мэнеджера кэша
     * (там стратегии хран€тс€ по weak-ссылке)
     */
    private Reference<MxLayeredStrategy<T>> selfReference;

    private Object[] data;

    private int queries;
    private int lastQueryTime;

    private int count;

    private T shorttimeValue;

    private MxReusageForecastManager<T> reusageForecastManager;

    private float reusageForecast;

    void updateReusageForecast() {
        reusageForecast = reusageForecastManager.getReusageForecast(this);
    }

    Reference<MxLayeredStrategy<T>> getSelfReference() {
        return selfReference;
    }

    void setSelfReference(Reference<MxLayeredStrategy<T>> selfReference) {
        this.selfReference = selfReference;
    }

    /**
     * should be called with synchronization on manager.
     *
     * @return true if this strategy should be preserved
     */
    boolean isConfident() {
        if (reusageForecast >= MIN_REUSAGE_TO_PRESERVE || shorttimeValue != null) {
            return true;
        }
        for (int i = 0; i < count - 1; i++) {
            if (data[i] != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        MxObjectOutput output = (MxObjectOutput) out;
        output.serialize(manager);
        output.serialize(getValue());
        output.writeObject(reusageForecastManager);
    }

    @Override
    @SuppressWarnings({"unchecked"})
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        MxObjectInput input = (MxObjectInput) in;
        MxLayeredCache<T> manager = input.deserialize();

        T stv = (T) input.deserialize();
        reusageForecastManager = (MxReusageForecastManager) input.readObject();

        init(manager, stv);
    }

    private void init(MxLayeredCache<T> manager, T stv) {
        this.manager = manager;
        count = manager.getLayerCount();
        data = new Object[count];

        synchronized (manager) {
            this.manager.registerStrategy(this);
            this.manager.addAndUpdate(this);
            shorttimeValue = stv;
        }
    }

    /**
     * @deprecated externalizable use only
     */
    @Deprecated
    public MxLayeredStrategy() {
    }

    public MxLayeredStrategy(MxLayeredCache<T> manager, T value, MxReusageForecastManager<T> reusageForecastManager) {
        this.reusageForecastManager = reusageForecastManager;
        init(manager, value);
    }

    /**
     * Ётот метод можно примен€ть, только если вы точно уверены, что этот MxLayeredStrategy больше не понадобитс€
     * (например, если утер€на ссылка на соответствующий ей прокси-объект).
     * ¬ противном случае, могут возникнуть непон€тные ошибки.
     */
    void removeFromAllCaches() {
        if (shorttimeValue != null) {
            shorttimeValue = null;
            manager.removeFromShorttime(this);
        }
        for (int i = 0; i < count - 1; i++) {
            if (data[i] != null) {
                manager.getLayer(i).removeFromCache(this);
                data[i] = null;
            }
        }
        data[count - 1] = null;
    }

    void clearShorttime() {
        if (convertToClear()) {
            shorttimeValue = null;
        }
    }

    private boolean convertToClear() {
        if (data[0] != null) {
            return true;
        }
        if (shorttimeValue instanceof MxProxy) {
            logger.warn("MxProxy passed to convertDown(int,Object)");
        }
        if (count == 1 || manager.getLayer(0).tryToCache(this)) {
            data[0] = shorttimeValue;
            return true;
        }
        if (convertDown(0, shorttimeValue)) {
            return true;
        }
        for (int i = 1; i<count; i++) {
            if (data[i] != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public T getValue() {
        DependencyNode callerNode = DependencyTracker.track(DependencyTracker.DUMMY_NODE);
        try {
            while (true) {
                try {
                    return getValue0();
                } catch (ResourceOccupied e) {
                    if (callerNode != null) {
                        throw e;
                    } else {
                        e.getResource().waitForEndOfModification();
                    }
                }
            }
        } finally {
            DependencyTracker.exit(callerNode);
        }
    }

    @SuppressWarnings({"unchecked"})
    private T getValue0() {
        synchronized (manager) {
            queries++;
            lastQueryTime = manager.getTime();
            if (shorttimeValue == null) {
                T v = (T)data[0];
                if (v == null) {
                    long start = System.nanoTime();
                    shorttimeValue = getInternalValue();
                    manager.addAndUpdate(this);
                    long end = System.nanoTime();
                    manager.miss(end - start);
                } else {
                    shorttimeValue = v;
                    manager.addAndUpdate(this);
                    manager.hit();
                }
            } else {
                if (isInList()) {
                    // элемент может не быть в списке, если его не удалось сохранить на диск - в этом случае значение
                    // будет установленно, но из списка он может быть удален
                    manager.moveToEnd(this);
                }
                manager.hit();
            }
            return shorttimeValue;
        }
    }

    void exitPool(int layer) {
        if (convertDown(layer, data[layer])) {
            // если не получаетс€ сконвертировать, то забьем на это дело, и оставим все как есть
            data[layer] = null;
        } else {
            // если есть хоть кака€-то форма, то можно удалить все равно
            for (int i = 0; i < count; i++) {
                if (data[i] != null && i != layer) {
                    data[layer] = null;
                    break;
                }
            }
        }
    }

    /**
     * Ќаходит оптимальный слой дл€ перехода, который обеспечит более компактное представление, чем имеетс€ сейчас,
     * при этом обеспечит максимально быстрый доступ, и переходит.
     *
     * @param layerId текущий слой
     * @param value   значение
     * @return true if corresponding layer was found and previous representation can be removed
     */
    private boolean convertDown(int layerId, Object value) {
        if (value instanceof MxProxy) {
            logger.warn("MxProxy passed to convertDown(int,Object)");
        }
        float minCost = Float.POSITIVE_INFINITY;
        int minLayer = -1;
        for (int i = layerId + 1; i < count; i++) {
            MxCacheLayer layer = manager.getLayer(i);
            if (data[i] != null || (manager.getConverter().canConvert(layerId, i) && (i == count - 1 || layer.canCache(this)))) {
                float cost = reusageForecast * manager.getConverter().getConvertCost(i, 0, 0);
                if (data[i] == null) {
                    cost += manager.getConverter().getConvertCost(layerId, i, 0);
                }
                cost /= layer.getPreferenceFactor();
                if (minCost > cost) {
                    minCost = cost;
                    minLayer = i;
                }
            }
        }
        if (minLayer == -1) {
            logger.error("Cannot collapse: no layer found to move from " + layerId);
            return false;
        }
        if (data[minLayer] == null) {
            if (minLayer != count - 1) {
                if (!manager.getLayer(minLayer).tryToCache(this)) {
                    logger.error("Cannot add to longtime cache from " + layerId);
                    return false;
                }
            }
            try {
                data[minLayer] = manager.getConverter().convert(layerId, minLayer, 0, value);
            } catch (Throwable e) {
                logger.error("Cannot convert from " + layerId + " to " + minLayer, e);
                return false;
            }
        }
        return true;
    }

    private T getInternalValue() {
        // data[0] is checked in getValue0(), so we don't check it again
        assert data[0] == null;
        int min = 0;
        float minCost = Float.POSITIVE_INFINITY;
        for (int i = 1; i < count; i++) {
            float cost = manager.getConverter().getConvertCost(i, 0, 0) / manager.getLayer(i).getPreferenceFactor();
            if (data[i] != null && cost < minCost) {
                min = i;
                minCost = cost;
            }
        }
        if (min == 0) {
            throw new MxCacheException("Cannot deconvert " + this + ": all layers are empty");
        }
        return convertAndSave(min);
    }

    @SuppressWarnings ({ "unchecked" })
    private T convertAndSave(int min) {
        T v = (T) manager.getConverter().convert(min, 0, 0, data[min]);
        if (manager.getLayer(0).tryToCache(this)) {
            data[0] = v;
        }
        return v;
    }

    public int getQueries() {
        return queries;
    }

    public int getLastQueryTime() {
        return lastQueryTime;
    }

    @Override
    public int compareTo(MxLayeredStrategy<T> o) {
        return Float.compare(reusageForecast, o.reusageForecast);
    }

    String getKey() {
        StringBuilder builder = new StringBuilder();
        if (shorttimeValue != null) {
            if (isInList()) {
                builder.append("SHORTTIME ");
            } else {
                builder.append("UNCLEAN ");
            }
        }
        for (int i = 0; i < count; i++) {
            if (data[i] != null) {
                builder.append(manager.getLayer(i).getName()).append(' ');
            }
        }
        return builder.toString();
    }

    MxLayeredCache<T> getManager() {
        return manager;
    }

    @Override
    public String toString() {
        return getKey();
    }
}
