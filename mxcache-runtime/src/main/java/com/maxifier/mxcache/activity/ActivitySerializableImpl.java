/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.activity;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class ActivitySerializableImpl implements Serializable {
    private static final long serialVersionUID = 100L;

    private final String name;

    public ActivitySerializableImpl(Activity activity) {
        this.name = activity.getName();
    }

    public Object readResolve() {
        return ActivityTracker.getActivity(name);
    }
}
