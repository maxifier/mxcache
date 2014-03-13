/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

import java.util.List;
import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface CacheControlMXBean {
    String getCacheProviderImpl();

    List<CacheInfo> getCaches();

    List<ResourceInfo> getResources();

    Map<String, List<CacheInfo>> getCachesByGroup();

    Map<String, List<CacheInfo>> getCachesByClass();

    Map<String, List<CacheInfo>> getCachesByTag();

    void clearByGroup(String group);

    void clearByTag(String tag);

    void clearByClass(String className) throws ClassNotFoundException;

    void clearByResource(String resourceName);
}
