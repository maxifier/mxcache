/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.abs.elementlocked;

import com.maxifier.mxcache.storage.Storage;

import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ElementLockedStorage extends Storage {
    Lock getLock();
}
