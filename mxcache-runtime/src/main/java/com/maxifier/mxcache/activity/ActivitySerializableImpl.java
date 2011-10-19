package com.maxifier.mxcache.activity;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 12:14:52
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
