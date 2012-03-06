package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import gnu.trove.THashMap;

import java.util.Collections;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 05.03.12
 * Time: 21:54
 */
public class InstrumentatorProvider {
    private static final Map<String, Instrumentator> INSTRUMENTATORS = createMap();

    private static Map<String, Instrumentator> createMap() {
        Map<String, Instrumentator> res = new THashMap<String, Instrumentator>();
        for (Instrumentator instrumentator : InstrumentatorImpl.VERSIONS) {
            res.put(instrumentator.getVersion(), instrumentator);
        }
        return Collections.unmodifiableMap(res);
    }

    public static Map<String, Instrumentator> getAvailableVersions() {
        return INSTRUMENTATORS;
    }
}
