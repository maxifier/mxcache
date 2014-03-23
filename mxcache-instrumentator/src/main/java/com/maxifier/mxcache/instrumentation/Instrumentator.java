/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Instrumentator {
    ClassInstrumentationResult instrument(byte[] bytecode) throws IllegalCachedClass;
    
    String getVersion();
}
