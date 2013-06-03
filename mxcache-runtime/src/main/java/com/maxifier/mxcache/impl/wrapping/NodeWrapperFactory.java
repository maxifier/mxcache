/* Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 * Maxifier Ltd  proprietary and confidential.
 * Use is subject to license terms.
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
