/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.util.HashWeakReference;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class SmartWeakReference<T> extends HashWeakReference<T> implements SmartReference {
    private Runnable callback;

    @SuppressWarnings({ "unchecked" })
    public SmartWeakReference(T referent) {
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
