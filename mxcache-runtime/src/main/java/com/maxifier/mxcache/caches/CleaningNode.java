/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.caches;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CleaningNode {
    /**
     * Invalidates the cache. <u>Must be non-blocking.</u> It should either clear the cache immediately
     * or mark it as dirty and clean on the next read/write operation.
     */
    void invalidate();
}
