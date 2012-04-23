package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.util.HashSoftReference;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.10.2010
* Time: 10:33:44
*/
class SmartSoftReference<T> extends HashSoftReference<T> implements SmartReference {
    private Runnable callback;

    @SuppressWarnings({ "unchecked" })
    public SmartSoftReference(T referent) {
        super(referent, SmartReferenceManager.<T>getReferenceQueue());
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
