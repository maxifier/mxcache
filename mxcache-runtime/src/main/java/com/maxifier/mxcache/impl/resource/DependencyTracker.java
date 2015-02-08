/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
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
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class DependencyTracker {
    private static final ThreadLocal<DependencyNode> NODE = new ThreadLocal<DependencyNode>();
    private static final ThreadLocal<TIdentityHashSet<ResourceViewable>> RESOURCE_VIEW_NODES = new ThreadLocal<TIdentityHashSet<ResourceViewable>>();

    /** ???? ???? ????????????, ???? ?? ????? ???????????????? ???????????? */
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

    public static TIdentityHashSet<ResourceViewable> saveResourceViewNodes(DependencyNode node) {
        TIdentityHashSet<ResourceViewable> oldNodes = RESOURCE_VIEW_NODES.get();
        RESOURCE_VIEW_NODES.set(DependencyTracker.getResourceViewDependentNodes(node));
        return oldNodes;
    }

    public static boolean isDependentResourceView(ResourceViewable node) {
        TIdentityHashSet<ResourceViewable> cleaningNodes = RESOURCE_VIEW_NODES.get();
        return cleaningNodes != null && cleaningNodes.contains(node);
    }

    public static void exitDependentResourceView(TIdentityHashSet<ResourceViewable> oldNodes) {
        RESOURCE_VIEW_NODES.set(oldNodes);
    }

    /**
     * Finds only dependent caches with ResourceView annotation.
     *
     * @param sourceNode initial dependency node to start searching dependent caches from
     * @return set of dependent caches
     */
    public static TIdentityHashSet<ResourceViewable> getResourceViewDependentNodes(DependencyNode sourceNode) {
        ResourceViewDeepVisitor visitor = new ResourceViewDeepVisitor();
        deepVisit(Collections.singleton(sourceNode), visitor);
        return visitor.getResult();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public void trackDependency(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            // ?????? ?? ??????, ???? ?????? ?? ??????????? ?????, ??? ??? ???? ????????
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

    private static class WrapDependencyNode extends AbstractDependencyNode {
        private final Collection<? extends CleaningNode> elements;

        public WrapDependencyNode(Collection<? extends CleaningNode> elements) {
            this.elements = elements;
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
