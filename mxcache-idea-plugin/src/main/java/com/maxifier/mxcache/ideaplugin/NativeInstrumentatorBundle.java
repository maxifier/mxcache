/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.Version;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class NativeInstrumentatorBundle implements InstrumentatorBundle {
    @Override
    public Map<String, Instrumentator> getAvailableVersions() {
        return InstrumentatorProvider.getAvailableVersions();
    }

    @Override
    public Version getVersion() {
        return MxCache.getVersionObject();
    }
}
