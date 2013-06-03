package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.InternalProbeFailedError;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.util.HashWeakReference;
import com.maxifier.mxcache.util.TIdentityHashSet;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 10:43:22
 */
public final class DependencyTracker {
    private static final ThreadLocal<DependencyNode> NODE = new ThreadLocal<DependencyNode>();

    /** ???? ???? ????????????, ???? ?? ????? ???????????????? ???????????? */
    public static final DependencyNode DUMMY_NODE = new DummyDependencyNode("<DUMMY>");

    public static final DependencyNode PROBE_NODE = new DummyDependencyNode("<PROBE>");

    public static final DependencyNode NOCACHE_NODE = new DummyDependencyNode("<NOCACHE>");

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

    public static void addExplicitDependency(DependencyNode node, MxResource resource) {
        if (resource instanceof MxResourceImpl) {
            ((MxResourceImpl) resource).trackDependency(node);
        } else {
            throw new UnsupportedOperationException("Current implementation of dependency tracker cannot deal with resource class " + resource.getClass());
        }
    }

    public static TIdentityHashSet<CleaningNode> getAllDependentNodes(Iterable<DependencyNode> src) {
        return getAllDependentNodes(src, Collections.<CleaningNode>emptySet());
    }

    public static TIdentityHashSet<CleaningNode> getAllDependentNodes(Iterable<DependencyNode> src, Collection<? extends CleaningNode> initial) {
        Set<DependencyNode> nodes = new THashSet<DependencyNode>();
        Queue<DependencyNode> queue = new LinkedList<DependencyNode>();

        DependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor(nodes, queue);

        // we enqueue this but we don't add it cause we don't want to call it's appendElements(Set)
        for (DependencyNode node : src) {
            node.visitDependantNodes(visitor);
        }
        while (!queue.isEmpty()) {
            queue.poll().visitDependantNodes(visitor);
        }
        TIdentityHashSet<CleaningNode> result = new TIdentityHashSet<CleaningNode>(nodes.size() + initial.size());
        result.addAll(initial);
        for (DependencyNode node : nodes) {
            node.appendNodes(result);
        }
        return result;
    }

    public static boolean isDummyNode(DependencyNode node) {
        return node instanceof DummyDependencyNode;
    }

    public static boolean isBypassCaches() {
        return NOCACHE_NODE.equals(get());
    }

    /**
     * Finds dependent caches taking ResourceView annotation into account.
     *
     * @param src list of dependency nodes to start searching dependent caches from
     * @return set of dependent caches
     */
    public static TIdentityHashSet<CleaningNode> getChangedDependentNodes(Iterable<DependencyNode> src) {
        Set<DependencyNode> nodes = new THashSet<DependencyNode>();
        Queue<DependencyNode> queue = new LinkedList<DependencyNode>();

        DependencyNodeVisitor visitor = new CollectingChangedDependencyNodeVisitor(nodes, queue);

        // we enqueue this but we don't add it cause we don't want to call it's appendElements(Set)
        for (DependencyNode node : src) {
            node.visitDependantNodes(visitor);
        }
        while (!queue.isEmpty()) {
            queue.poll().visitDependantNodes(visitor);
        }

        TIdentityHashSet<CleaningNode> result = new TIdentityHashSet<CleaningNode>(nodes.size());
        for (DependencyNode node : nodes) {
            node.appendNodes(result);
        }
        return result;
    }

    private static final class DummyDependencyNode implements DependencyNode {
        private final Reference<DependencyNode> selfReference = new HashWeakReference<DependencyNode>(this);
        private final String name;

        private DummyDependencyNode(String name) {
            this.name = name;
        }

        @Override
        public void visitDependantNodes(DependencyNodeVisitor visitor) {
            // do nothing, we don't track dependencies for that cache
        }

        @Override
        public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void trackDependency(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addNode(@NotNull CleaningNode cache) {
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

}
