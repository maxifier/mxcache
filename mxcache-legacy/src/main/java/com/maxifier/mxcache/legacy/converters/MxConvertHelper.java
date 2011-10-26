package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.legacy.MxResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.util.Formatter;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.02.2009
 * Time: 13:42:22
 */
public final class MxConvertHelper {
    private static final Logger logger = LoggerFactory.getLogger(MxConvertHelper.class);

    private static final int PREDEFINED_CALLS = 100;

    private final MxConvertType[] types;
    private final MxConvertState[] states;
    private final MxConverter[][] converters;
    private final int[][][] converts;
    private final long[][][] convertTime;
    private final float[][][] predefinedTimes;
    private final MxResource statistics;

    MxConvertHelper(MxResource statistics, MxConvertState[] states, MxConvertType[] types, float[][][] predefinedTimes, MxConverter[][] converters) {
        this.statistics = statistics;
        this.types = types;
        this.predefinedTimes = predefinedTimes;
        convertTime = new long[states.length][states.length][types.length];
        converts = new int[states.length][states.length][types.length];
        this.converters = converters;
        this.states = states;
    }

    public boolean canConvert(MxConvertState from, MxConvertState to) {
        return canConvert(from.getId(), to.getId());
    }

    public boolean canConvert(int i, int j) {
        return converters[i][j] != null;
    }

    @SuppressWarnings({"unchecked"})
    public <F, T> T convert(MxConvertState<F> from, MxConvertState<T> to, MxConvertType type, F o) {
        return (T) convert(from.getId(), to.getId(), type.getId(), o);
    }

    public Object convert(int from, int to, int type, Object o) throws ConverterException {
        if (from < 0 || from >= states.length) {
            throw new ConverterException("Invalid from layer: " + from);
        }
        if (to < 0 || to >= states.length) {
            throw new ConverterException("Invalid to layer: " + from);
        }
        MxConverter converter = converters[from][to];
        if (converter == null) {
            throw new ConverterException("Cannot convert from " + states[from] + " to " + states[to]);
        }
        long start = System.nanoTime();
        //noinspection unchecked
        Object r = converter.convert(o);
        long end = System.nanoTime();
        long duration = end - start;
        reportCost(from, to, type, duration);
        return r;
    }

    public void reportCost(MxConvertState from, MxConvertState to, MxConvertType type, long duration) {
        reportCost(from.getId(), to.getId(), type.getId(), duration);
    }

    public void reportCost(int from, int to, int type, long duration) {
        synchronized (this) {
            converts[from][to][type]++;
            convertTime[from][to][type] += Math.round(duration / 1000000.0);
        }
    }

    public float getConvertCost(MxConvertState from, MxConvertState to, MxConvertType type) {
        return getConvertCost(from.getId(), to.getId(), type.getId());
    }

    public float getConvertCost(int from, int to, int type) {
        int c = converts[from][to][type] + PREDEFINED_CALLS;
        return (convertTime[from][to][type] + PREDEFINED_CALLS * predefinedTimes[from][to][type]) / c;
    }

    public synchronized void flushStat() {
        try {
            DataOutputStream dos = new DataOutputStream(statistics.getOutputStream(false));
            try {
                dos.writeInt(states.length);
                dos.writeInt(types.length);
                for (int i = 0; i < states.length; i++) {
                    for (int j = 0; j < states.length; j++) {
                        for (int k = 0; k < types.length; k++) {
                            dos.writeFloat(getConvertCost(i, j, k));
                        }
                    }
                }
            } finally {
                dos.close();
            }
        } catch (Exception e) {
            logger.warn("Cannot flush stat to " + statistics);
        }
    }

    public void showStat() {
        for (int k = 0; k < types.length; k++) {
            MxConvertType type = types[k];
            if (type != null) {
                logger.debug("FOR TYPE " + type + ":");
                for (int i = 0; i < states.length; i++) {
                    for (int j = 0; j < states.length; j++) {
                        int cc = converts[i][j][k];
                        if (cc != 0) {
                            long ct = convertTime[i][j][k];
                            logger.debug(String.format("\tCONVERTS %s -> %s: %d times in %d (%.2f ms per convertion)", states[i], states[j], cc, ct, ((float) ct / cc)));
                        }
                    }
                }
            }
        }
    }

    public String getStatistics() {
        StringBuilder b = new StringBuilder();
        Formatter f = new Formatter(b);
        for (int k = 0; k < types.length; k++) {
            MxConvertType type = types[k];
            if (type != null) {
                f.format("FOR TYPE " + type + ":\n");
                for (int i = 0; i < states.length; i++) {
                    for (int j = 0; j < states.length; j++) {
                        int cc = converts[i][j][k];
                        float pt = predefinedTimes[i][j][k];
                        if (cc != 0 || pt != 0.0) {
                            long ct = convertTime[i][j][k];
                            f.format("\tCONVERTS %s -> %s: %d times in %d (%.2f ms per convertion) - %.2f ms predefined\n", states[i], states[j], cc, ct, ((float) ct / cc), pt);
                        }
                    }
                }
            }
        }
        return b.toString();
    }
}