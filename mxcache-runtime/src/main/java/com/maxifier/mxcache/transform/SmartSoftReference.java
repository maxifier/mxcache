package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.util.HashSoftReference;

import java.lang.ref.ReferenceQueue;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.10.2010
* Time: 10:33:44
*/
class SmartSoftReference<T> extends HashSoftReference<T> {
    private Runnable callback;

    @SuppressWarnings({ "unchecked" })
    public SmartSoftReference(T referent, ReferenceQueue queue) {
        super(referent, queue);
    }

    public Runnable getCallback() {
        return callback;
    }

    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
}
