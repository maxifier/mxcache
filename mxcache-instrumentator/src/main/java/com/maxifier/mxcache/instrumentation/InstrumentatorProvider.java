/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.instrumentation.current.InstrumentatorImpl;
import gnu.trove.THashMap;

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
}
