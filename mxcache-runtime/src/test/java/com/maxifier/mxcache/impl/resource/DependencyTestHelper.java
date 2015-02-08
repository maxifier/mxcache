/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;
import gnu.trove.set.hash.THashSet;

import java.util.Collections;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class DependencyTestHelper {
    /**
     * @param node resource
     * @return список всех узлов, зависящих от данного, в том числе транзитивные зависимости.
     */
    public static Set<DependencyNode> getAllDependentNodes(MxResource node) {
        return getAllDependentNodes((DependencyNode)node);
    }

    /**
     * @param node node
     * @return список всех узлов, зависящих от данного, в том числе транзитивные зависимости.
     */
    public static Set<DependencyNode> getAllDependentNodes(DependencyNode node) {
        Set<DependencyNode> nodes = new THashSet<DependencyNode>();
        DependencyTracker.deepVisit(Collections.singleton(node), new CollectingDeepVisitor(nodes));
        return nodes;
    }
}
