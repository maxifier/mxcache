/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.impl.resource.MxResourceFactory;
import com.maxifier.mxcache.interfaces.Statistics;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CacheProvider;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheControl implements CacheControlMXBean {
    private final CacheProvider provider;

    public CacheControl(@Nonnull CacheProvider provider) {
        this.provider = provider;
    }

    @Override
    public String getCacheProviderImpl() {
        return getDisplayName(provider.getClass());
    }

    @Override
    public List<ResourceInfo> getResources() {
        Set<MxResource> resources = MxResourceFactory.getAllResources();
        List<ResourceInfo> res = new ArrayList<ResourceInfo>(resources.size());
        for (MxResource resource : resources) {
            res.add(new ResourceInfo(resource.getName()));
        }
        return res;
    }

    @Override
    public List<CacheInfo> getCaches() {
        List<CacheManager> caches = provider.getCaches();
        List<CacheInfo> res = new ArrayList<CacheInfo>(caches.size());
        for (CacheManager<?> cacheManager : caches) {
            String impl;
            try {
                impl = cacheManager.getImplementationDetails();
            } catch (RuntimeException e) {
                impl = "Error: " + e.getMessage();
            }
            int count = 0;
            int total = 0;
            int totalHits = 0;
            int totalMisses = 0;
            double averageCalculation = 0.0;
            Set<Statistics> statisticsSet = new TIdentityHashSet<Statistics>();
            try {
                for (Cache c : CacheFactory.getCaches(cacheManager.getDescriptor())) {
                    count++;
                    total += c.getSize();
                    Statistics stat = c.getStatistics();
                    if (stat != null) {
                        statisticsSet.add(stat);
                    }
                }
            } catch (Exception e) {
                // ignore it, if a cacheManager fails to get it's data, it's not our problem
            }
            for (Statistics statistics : statisticsSet) {
                totalHits += statistics.getHits();
                totalMisses += statistics.getMisses();
                averageCalculation  += statistics.getTotalCalculationTime();
            }
            CacheDescriptor<?> descriptor = cacheManager.getDescriptor();
            Class<?> ownerClass = descriptor.getOwnerClass();
            CacheContext context = cacheManager.getContext();
            res.add(new CacheInfo(context == null ? "<no context>" : context.toString(), descriptor.getKeyType() == null ? null : getDisplayName(descriptor.getKeyType()),
                                  getDisplayName(descriptor.getValueType()),
                                  descriptor.getMethod().toGenericString(),
                                  descriptor.getCacheName(), descriptor.getId(), count, total,
                                  descriptor.getGroup(),
                                  descriptor.getTags(),
                                  impl,
                                  getDisplayName(ownerClass),
                                  totalHits, totalMisses, totalMisses == 0 ? 0.0 : averageCalculation / totalMisses));
        }
        return res;
    }

    @Override
    public Map<String, List<CacheInfo>> getCachesByGroup() {
        Map<String, List<CacheInfo>> res = new THashMap<String, List<CacheInfo>>();
        for (CacheInfo cacheInfo : getCaches()) {
            String group = cacheInfo.getGroup();
            List<CacheInfo> list = res.get(group);
            if (list == null) {
                list = new ArrayList<CacheInfo>();
                res.put(group, list);
            }
            list.add(cacheInfo);
        }
        return res;
    }

    @Override
    public Map<String, List<CacheInfo>> getCachesByClass() {
        Map<String, List<CacheInfo>> res = new THashMap<String, List<CacheInfo>>();
        for (CacheInfo cacheInfo : getCaches()) {
            String owner = cacheInfo.getOwner();
            List<CacheInfo> list = res.get(owner);
            if (list == null) {
                list = new ArrayList<CacheInfo>();
                res.put(owner, list);
            }
            list.add(cacheInfo);
        }
        return res;
    }

    @Override
    public Map<String, List<CacheInfo>> getCachesByTag() {
        Map<String, List<CacheInfo>> res = new THashMap<String, List<CacheInfo>>();
        for (CacheInfo cacheInfo : getCaches()) {
            String[] tags = cacheInfo.getTags();
            if (tags != null) {
                for (String tag : tags) {
                    List<CacheInfo> list = res.get(tag);
                    if (list == null) {
                        list = new ArrayList<CacheInfo>();
                        res.put(tag, list);
                    }
                    list.add(cacheInfo);
                }
            }
        }
        return res;
    }

    @Override
    public void clearByGroup(String group) {
        CacheFactory.getCleaner().clearCacheByGroup(group);
    }

    @Override
    public void clearByTag(String tag) {
        CacheFactory.getCleaner().clearCacheByGroup(tag);
    }

    @Override
    public void clearByClass(String className) throws ClassNotFoundException {
        CacheFactory.getCleaner().clearCacheByClass(Class.forName(className));
    }

    @Override
    public void clearByResource(String resourceName) {
        MxResourceFactory.getResource(resourceName).clearDependentCaches();
    }

    private static String getDisplayName(Class<?> ownerClass) {
        String canonicalName = ownerClass.getCanonicalName();
        return canonicalName == null ? ownerClass.getName() : canonicalName;
    }
}
