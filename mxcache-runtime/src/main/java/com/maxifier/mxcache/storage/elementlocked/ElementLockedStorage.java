/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.Storage;

import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface ElementLockedStorage extends Storage {
    Lock getLock();
}
