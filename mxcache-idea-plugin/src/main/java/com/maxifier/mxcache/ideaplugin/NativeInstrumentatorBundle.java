package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.Version;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 06.03.12
 * Time: 13:16
 */
public class NativeInstrumentatorBundle implements InstrumentatorBundle {
    @Override
    public Map<String, Instrumentator> getAvailableVersions() {
        return InstrumentatorProvider.getAvailableVersions();
    }

    @Override
    public Version getVersion() {
        return MxCache.getVersion();
    }
}
