/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import java.util.Queue;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface DependencyNodeVisitor {
    void visit(DependencyNode node);

    Set<DependencyNode> getNodes();

    Queue<DependencyNode> getQueue();
}
