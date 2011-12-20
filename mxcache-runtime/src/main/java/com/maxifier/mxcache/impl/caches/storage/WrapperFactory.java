package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.impl.resource.DependencyNode;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.09.2010
 * Time: 19:40:03
 */
public interface WrapperFactory {
    Cache wrap(Object owner, Object calculable, Storage storage, MutableStatistics statistics);
}
