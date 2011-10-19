package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxResource;
import com.maxifier.mxcache.legacy.MxResourceManager;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.02.2009
 * Time: 13:44:51
 */
public final class MxConvertHelperBuilder {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertHelperBuilder.class);

    private final MxResourceManager rm;

    private final MxConvertType[] types;
    private final MxConvertState[] states;
    private final String name;
    private final ConverterInfo[][] converters;

    public MxConvertHelperBuilder(MxResourceManager rm, String name, MxConvertType[] types, MxConvertState... states) {
        this.rm = rm;

        this.name = name;
        this.types = createTypesArray(types);
        this.states = createStatesArray(states);

        converters = new ConverterInfo[states.length][states.length];
    }

    private static MxConvertType[] createTypesArray(MxConvertType[] types) {
        int maxId = maxId(types);
        MxConvertType[] res = new MxConvertType[maxId + 1];
        for (MxConvertType type : types) {
            int id = type.getId();
            if (res[id] != null) {
                throw new IllegalArgumentException("Both " + type + " and " + res[id] + " has ID = " + id);
            }
            res[id] = type;
        }
        return res;
    }

    private static MxConvertState[] createStatesArray(MxConvertState[] states) {
        int maxId = maxId(states);
        MxConvertState[] res = new MxConvertState[maxId + 1];
        for (MxConvertState state : states) {
            int id = state.getId();
            if (res[id] != null) {
                throw new IllegalArgumentException("Both " + state + " and " + res[id] + " has ID = " + id);
            }
            res[id] = state;
        }
        return res;
    }

    private static int maxId(MxConvertType[] types) {
        int maxId = 0;
        for (MxConvertType type : types) {
            int id = type.getId();
            if (maxId < id) {
                maxId = id;
            }
        }
        return maxId;
    }

    private static int maxId(MxConvertState[] states) {
        int maxId = 0;
        for (MxConvertState state : states) {
            int id = state.getId();
            if (maxId < id) {
                maxId = id;
            }
        }
        return maxId;
    }

    private static class ConverterInfo {
        private final float avgLoadTime;
        private final MxConverter converter;

        private ConverterInfo(MxConverter converter, float avgLoadTime) {
            this.avgLoadTime = avgLoadTime;
            this.converter = converter;
        }

        public float getAvgLoadTime() {
            return avgLoadTime;
        }

        public MxConverter getConverter() {
            return converter;
        }
    }

    public synchronized <F, T> void addConverter(MxConvertState<F> from, MxConvertState<T> to, @NotNull MxConverter<? extends F, ? extends T> converter, float avgLoadTime) {
        int fid = from.getId();
        int tid = to.getId();
        if (fid >= states.length || states[fid] == null) {
            throw new IllegalArgumentException("No such state: from = " + from);
        }
        if (tid >= states.length || states[tid] == null) {
            throw new IllegalArgumentException("No such state: to = " + to);
        }
        if (converters[fid][tid] != null) {
            throw new IllegalStateException("Converter is already bound: " + states[fid] + "->" + states[tid]);
        }
        converters[fid][tid] = new ConverterInfo(converter, avgLoadTime);
    }

    @SuppressWarnings("unchecked")
    public MxConvertHelper build() {
        MxResource statisticsResource = rm.getTempResource("mxcache-io-stat/" + name.replaceAll("[^\\w]+", "") + ".mxc");

        float[][][] stat = readStatistics(statisticsResource);

        MxConverter[][] nc = new MxConverter[states.length][states.length];
        float[][][] convertTime = fillConverters(stat, nc);
        return new MxConvertHelper(statisticsResource, states, types, convertTime, nc);
    }

    private float[][][] fillConverters(float[][][] stat, MxConverter[][] nc) {
        float[][][] convertTime = new float[states.length][states.length][types.length];
        for (int i = 0; i < states.length; i++) {
            for (int j = 0; j < states.length; j++) {
                ConverterInfo c = converters[i][j];
                if (c != null) {
                    nc[i][j] = c.getConverter();
                    if (stat == null) {
                        Arrays.fill(convertTime[i][j], c.getAvgLoadTime());
                    } else {
                        System.arraycopy(stat[i][j], 0, convertTime[i][j], 0, types.length);
                    }
                }
            }
        }
        return convertTime;
    }

    private float[][][] readStatistics(MxResource statisticsResource) {
        if (statisticsResource.exists()) {
            try {
                return readStatistics0(statisticsResource);
            } catch (Exception e) {
                logger.warn("Statistics in " + statisticsResource + " is corrupted", e);
                return null;
            }
        } else {
            return null;
        }
    }

    private float[][][] readStatistics0(MxResource statisticsResource) throws IOException {
        DataInputStream dis = new DataInputStream(statisticsResource.getInputStream());
        try {
            int nx = dis.readInt();
            int mx = dis.readInt();
            if (states.length == nx && types.length == mx) {
                float[][][] stat = new float[states.length][states.length][types.length];
                for (int i = 0; i < states.length; i++) {
                    for (int j = 0; j < states.length; j++) {
                        for (int k = 0; k < types.length; k++) {
                            stat[i][j][k] = dis.readFloat();
                        }
                    }
                }
                return stat;
            }
            logger.warn("Statistics in " + statisticsResource + " is corrupted (has wrong size)");
            return null;
        } finally {
            dis.close();
        }
    }
}
