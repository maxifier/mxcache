/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface MxCacheConfigProvider {
    Rule getRule(Class className, String group, String[] tags);

    List<ResourceConfig> getResources();
}
