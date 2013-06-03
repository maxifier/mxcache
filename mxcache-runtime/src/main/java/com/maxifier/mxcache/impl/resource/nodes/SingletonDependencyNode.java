package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;
import com.maxifier.mxcache.util.TIdentityHashSet;

import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 12:32:45
 */
public class SingletonDependencyNode extends AbstractDependencyNode {
    // we don't need to store reference here cause this node exists only if object itself is not gc'ed
    protected CleaningNode instance;

    public SingletonDependencyNode() {
        // do nothing - instance is set by addNode.
    }

    public SingletonDependencyNode(CleaningNode instance) {
        this.instance = instance;
    }

    @Override
    public synchronized void addNode(@NotNull CleaningNode cache) {
        if (instance != null) {
            throw new UnsupportedOperationException("Singleton dependency node should has only one cache");
        }
        instance = cache;
    }

    @Override
    public synchronized void appendNodes(TIdentityHashSet<CleaningNode> elements) {
        elements.add(instance);
    }

    @Override
    public String toString() {
        return "DependencyNode<" + instance + ">";
    }
}
