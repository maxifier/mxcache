/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface SmartReference {
    Runnable getCallback();

    void setCallback(Runnable callback);
}
