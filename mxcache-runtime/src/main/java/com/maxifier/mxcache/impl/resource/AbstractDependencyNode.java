package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.util.HashWeakReference;
import gnu.trove.THashSet;
import javax.annotation.Nullable;

import java.lang.ref.Reference;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 17:20:01
 */
public abstract class AbstractDependencyNode implements DependencyNode {
    /**
     * Set of dependent nodes. It may be null cause there is no need to allocate whole set for each node.
     */
    private Set<Reference<DependencyNode>> dependentNodes;

    private Reference<DependencyNode> selfReference;

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

    /**
     * @return примерный размер (примерный - потому что может включать некоторые мертвые ссылки)
     */
    public synchronized int getApproxSize() {
        return dependentNodes == null ? 0 : dependentNodes.size();
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

    protected static boolean equal(@Nullable Object a, @Nullable Object b) {
        return a == b || (a != null && a.equals(b));
    }
}
