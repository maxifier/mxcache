/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.InternalProbeFailedError;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.nodes.ResourceViewable;
import com.maxifier.mxcache.util.HashWeakReference;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.map.hash.TCustomHashMap;
import gnu.trove.strategy.IdentityHashingStrategy;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * DependencyTracker tracks dependencies between caches.
 *
 * Each cache has a DependencyNode associated with it. The dependency node may be shared across few cache instances
 * (e.g. all instances of certain method share a single dependency node) or created for each cache instance
 * individually. DependencyNodes form a directed graph where edge from A to B means that all caches associated with B
 * depend on caches associated with A and should be invalidated once any of caches associated with A are invalidated.
 *
 * When a cached method enters a stack, it does the following:
 * <ul>
 * <li>gets the top dependency node from {@link #NODE} and adds an edge to dependency graph from its own node to this
 * top node;</li>
 * <li>puts a reference to its dependency node to {@link #NODE}.</li>
 * </ul>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class DependencyTracker {
    private static final ThreadLocal<DependencyNode> NODE = new ThreadLocal<DependencyNode>();

    public static final DependencyNode DUMMY_NODE = new DummyDependencyNode("<DUMMY>");

    public static final DependencyNode PROBE_NODE = new DummyDependencyNode("<PROBE>");
    public static final DependencyNode NOCACHE_NODE = new DummyDependencyNode("<NOCACHE>");
    public static final DependencyNode HIDDEN_CALLER_NODE = new HiddenCallerDependencyNode();

    private DependencyTracker() {
    }

    /**
     * This method marks current top of stack dependency node as dependant of given node; it doesn't push given node to
     * stack. It is equivalent to <code>exit(track(node))</code> sequence, but works much faster.
     *
     * @param node dependency node
     */
    public static void mark(DependencyNode node) {
        if (node != DUMMY_NODE) {
            track(node, NODE.get());
        }
    }

    /**
     * This method pushes given node to top of stack, and marks current on-stack node as dependant of given node.
     * This method returns current stack top; later it should be passed as an argument to
     * {@link DependencyTracker#exit(DependencyNode)}.
     *
     * @param node node to push
     * @return previous on-stack dependency node
     */
    public static DependencyNode track(DependencyNode node) {
        DependencyNode oldNode = NODE.get();
        assert oldNode == null || node != null : "Could not reassign node to null";
        if (oldNode == PROBE_NODE) {
            throw new InternalProbeFailedError();
        }
        if (node != DUMMY_NODE) {
            track(node, oldNode);
            NODE.set(node);
        } else if (oldNode == null) {
            // ???? ???? ???? ??????, ?? ???-???? ???????? ??? ????-?????????
            NODE.set(node);
        }
        return oldNode;
    }

    private static void track(DependencyNode node, DependencyNode oldNode) {
        if (oldNode != null && oldNode != DUMMY_NODE && node != oldNode) {
            node.trackDependency(oldNode);
        }
    }

    public static void exit(@Nullable DependencyNode callerNode) {
        NODE.set(callerNode);
    }

    public static DependencyNode get() {
        return NODE.get();
    }

    public static boolean hasUnderlyingNode() {
        return NODE.get() != null;
    }

    public static void deepVisit(Iterable<DependencyNode> src, DependencyNode.Visitor visitor) {
        for (DependencyNode node : src) {
            visitor.visit(node);
        }
        Queue<DependencyNode> queue = visitor.getQueue();
        while (!queue.isEmpty()) {
            queue.poll().visitDependantNodes(visitor);
        }
    }

    public static boolean isDummyNode(DependencyNode node) {
        return node instanceof DummyDependencyNode;
    }

    public static boolean isBypassCaches() {
        return NOCACHE_NODE.equals(get());
    }

    public static void deepInvalidate(DependencyNode node) {
        deepVisit(Collections.singleton(node), new InvalidateAllVisitor());
    }

    public static void deepInvalidateWithResourceView(DependencyNode node) {
        deepVisit(Collections.singleton(node), new InvalidateChangedVisitor());
    }

    public static void deepInvalidate(final Collection<? extends CleaningNode> elements) {
        deepInvalidate(new WrapDependencyNode(elements));
    }

    private static final class DummyDependencyNode implements DependencyNode {
        private final Reference<DependencyNode> selfReference = new HashWeakReference<DependencyNode>(this);
        private final String name;

        private DummyDependencyNode(String name) {
            this.name = name;
        }

        @Override
        public void visitDependantNodes(Visitor visitor) {
            // do nothing, we don't track dependencies for that cache
        }

        @Override
        public void invalidate() {
            // do nothing, we don't track dependencies for that cache
        }

        @Override
        public void trackDependency(DependencyNode node) {
            // do nothing, we don't track dependencies for that cache
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            // do nothing, we don't track dependencies for that cache
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public Reference<DependencyNode> getSelfReference() {
            return selfReference;
        }
    }

    private static class HiddenCallerDependencyNode implements DependencyNode {
        private final Reference<DependencyNode> thisReference = new WeakReference<DependencyNode>(this);

        @Override
        public Reference<DependencyNode> getSelfReference() {
            return thisReference;
        }

        @Override
        public void visitDependantNodes(Visitor visitor) {
            // do nothing
        }

        @Override
        public void invalidate() {
            // do nothing
        }

        @Override
        public void trackDependency(DependencyNode node) {
            // do nothing
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            // do nothing
        }

        @Override
        public String toString() {
            return "HiddenCaller - see MxCache.hideCallerDependencies";
        }
    }

    private static class WrapDependencyNode implements DependencyNode {
        private final Collection<? extends CleaningNode> elements;

        public WrapDependencyNode(Collection<? extends CleaningNode> elements) {
            this.elements = elements;
        }

        @Override
        public Reference<DependencyNode> getSelfReference() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void visitDependantNodes(Visitor visitor) {
            for (CleaningNode element : elements) {
                visitor.visit(element.getDependencyNode());
            }
        }

        @Override
        public void trackDependency(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void invalidate() {
            for (CleaningNode element : elements) {
                element.invalidate();
            }
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }
    }

    private static class InvalidateAllVisitor implements DependencyNode.Visitor {
        private final Queue<DependencyNode> queue = new LinkedList<DependencyNode>();
        private final TIdentityHashSet<DependencyNode> visitedNodes = new TIdentityHashSet<DependencyNode>();

        @Override
        public Queue<DependencyNode> getQueue() {
            return queue;
        }

        @Override
        public void visit(DependencyNode node) {
            if (visitedNodes.add(node)) {
                queue.add(node);
                node.invalidate();
            }
        }
    }

    private static class InvalidateChangedVisitor implements DependencyNode.Visitor {
        private final Queue<DependencyNode> queue = new LinkedList<DependencyNode>();
        private final TIdentityHashSet<DependencyNode> visitedNodes = new TIdentityHashSet<DependencyNode>();
        private final Map<ResourceViewable, Boolean> changedCache = new TCustomHashMap<ResourceViewable, Boolean>(IdentityHashingStrategy.INSTANCE);

        private boolean isChanged(ResourceViewable resourceViewable) {
            Boolean res = changedCache.get(resourceViewable);
            if (res == null) {
                res = resourceViewable.isChanged();
                changedCache.put(resourceViewable, res);
            }
            return res;
        }

        @Override
        public Queue<DependencyNode> getQueue() {
            return queue;
        }

        @Override
        public void visit(DependencyNode node) {
            if (visitedNodes.add(node) && (!(node instanceof ResourceViewable) || isChanged((ResourceViewable) node))) {
                queue.add(node);
                node.invalidate();
            }
        }
    }
}
