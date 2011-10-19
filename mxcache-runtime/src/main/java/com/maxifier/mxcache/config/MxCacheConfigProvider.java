package com.maxifier.mxcache.config;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.10.2010
 * Time: 14:25:39
 */
public interface MxCacheConfigProvider {
    Rule getRule(Class className, String group, String[] tags);

    List<ResourceConfig> getResources();
}
