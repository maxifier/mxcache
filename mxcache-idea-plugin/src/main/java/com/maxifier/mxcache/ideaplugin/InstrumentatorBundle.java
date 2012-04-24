package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.Version;
import com.maxifier.mxcache.instrumentation.Instrumentator;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 06.03.12
 * Time: 13:15
 */
public interface InstrumentatorBundle {
    Map<String, Instrumentator> getAvailableVersions();
    
    Version getVersion();
}
