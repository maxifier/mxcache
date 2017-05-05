/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.scheduler;

import com.maxifier.mxcache.PublicAPI;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface MxScheduler {
    @PublicAPI
    void schedule(Runnable runnable);
}
