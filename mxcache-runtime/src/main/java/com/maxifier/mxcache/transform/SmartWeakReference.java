package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.util.HashWeakReference;

import java.lang.ref.ReferenceQueue;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.10.2010
* Time: 10:33:40
*/
class SmartWeakReference<T> extends HashWeakReference<T> implements SmartReference {
    private Runnable callback;

    @SuppressWarnings({ "unchecked" })
    public SmartWeakReference(T referent, ReferenceQueue queue) {
        super(referent, queue);
    }

    @Override
    public Runnable getCallback() {
        return callback;
    }

    @Override
    public void setCallback(Runnable callback) {
        this.callback = callback;
    }
}
