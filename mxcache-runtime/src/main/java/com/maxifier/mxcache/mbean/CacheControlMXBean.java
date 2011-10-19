package com.maxifier.mxcache.mbean;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 07.04.2010
 * Time: 15:28:06
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
