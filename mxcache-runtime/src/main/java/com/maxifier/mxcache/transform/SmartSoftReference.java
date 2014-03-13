/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.util.HashSoftReference;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
