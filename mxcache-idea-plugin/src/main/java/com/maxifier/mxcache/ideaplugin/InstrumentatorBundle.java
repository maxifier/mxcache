/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin;

import com.maxifier.mxcache.Version;
import com.maxifier.mxcache.instrumentation.Instrumentator;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface InstrumentatorBundle {
    Map<String, Instrumentator> getAvailableVersions();
    
    Version getVersion();
}
