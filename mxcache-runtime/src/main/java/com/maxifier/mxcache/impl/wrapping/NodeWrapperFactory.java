/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.wrapping;

import com.maxifier.mxcache.caches.Calculable;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;

/**
 * @author Elena Saymanina (elena.saymanina@maxifier.com) (30.05.13)
 */
public interface NodeWrapperFactory {
    AbstractDependencyNode wrap();
}
