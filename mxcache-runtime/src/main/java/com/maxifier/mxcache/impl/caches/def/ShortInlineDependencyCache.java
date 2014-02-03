package com.maxifier.mxcache.impl.caches.def;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.MutableStatistics;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNodeVisitor;
import com.maxifier.mxcache.util.HashWeakReference;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;

import java.lang.ref.Reference;
import java.util.Iterator;
import java.util.Set;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
public class ShortInlineDependencyCache extends ShortInlineCacheImpl implements DependencyNode {
    /**
     * Set of dependent nodes. It may be null cause there is no need to allocate whole set for each node.
     */
    private Set<Reference<DependencyNode>> dependentNodes;

    private Reference<DependencyNode> selfReference;

    public ShortInlineDependencyCache(Object owner, ShortCalculatable calculable, MutableStatistics statistics) {
        super(owner, calculable, statistics);
        setDependencyNode(this);
    }

    public synchronized void visitDependantNodes(DependencyNodeVisitor visitor) {
        if (dependentNodes != null) {
            for (Iterator<Reference<DependencyNode>> it = dependentNodes.iterator(); it.hasNext();) {
                Reference<DependencyNode> ref = it.next();
                DependencyNode instance = ref.get();
                if (instance != null) {
                    visitor.visit(instance);
                } else {
                    it.remove();
                }
            }
        }
    }

    @Override
    public synchronized Reference<DependencyNode> getSelfReference() {
        if (selfReference == null) {
            selfReference = new HashWeakReference<DependencyNode>(this);
        }
        return selfReference;
    }

    @Override
    public synchronized void trackDependency(DependencyNode node) {
        if (dependentNodes == null) {
            dependentNodes = new THashSet<Reference<DependencyNode>>();
        }
        dependentNodes.add(node.getSelfReference());
    }

    @Override
    public synchronized void addNode(@Nonnull CleaningNode cache) {
        throw new UnsupportedOperationException("Inline dependency node should has only one cache");
    }

    @Override
    public synchronized void appendNodes(TIdentityHashSet<CleaningNode> elements) {
        elements.add(this);
    }
}
