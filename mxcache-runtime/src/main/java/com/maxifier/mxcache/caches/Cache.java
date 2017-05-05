/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.caches;

import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import com.maxifier.mxcache.provider.CacheDescriptor;

/**
 * The parent of all caches, contains some basic operations.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Cache extends StatisticsHolder, CleaningNode {

    /**
     * This method will obtain a lock internally if it is required.
     *
     * @return the approximate size of cache (number of elements)
     */
    int getSize();

    /**
     * @return cache descriptor
     */
    CacheDescriptor getDescriptor();

    void setDependencyNode(DependencyNode node);
}
