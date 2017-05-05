/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import gnu.trove.map.hash.THashMap;

import java.util.Collections;
import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class InstrumentatorProvider {
    private InstrumentatorProvider() {}

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

    public static Instrumentator getPreferredVersion() {
        return getAvailableVersions().get(MxCache.getCompatibleVersion());
    }

    public static Instrumentator getExactVersion(String version) {
        return getAvailableVersions().get(version);
    }
}
