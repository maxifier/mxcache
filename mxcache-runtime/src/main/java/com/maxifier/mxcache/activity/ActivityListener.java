/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.activity;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ActivityListener {
    void started(ActivityScope scope);

    void finished(ActivityScope scope);
}
