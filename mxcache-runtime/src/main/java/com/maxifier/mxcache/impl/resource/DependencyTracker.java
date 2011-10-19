package com.maxifier.mxcache.impl.resource;

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

    /** Этот узел выставляется, если не нужно автоотслеживание зависимостей */
    public static final DependencyNode DUMMY_NODE = new DummyDependencyNode();

    private DependencyTracker() {
    }

    /**
     * This method marks current top of stack dependency node as dependant of given node; it doesn't push given node to
     * stack. It is equivalent to <code>exit(track(node))</code> sequence, but works much faster.
     * @param node dependency node
     */
    public static void mark(DependencyNode node) {
        if (node != DUMMY_NODE) {
            DependencyNode oldNode = NODE.get();
            if (oldNode != null && oldNode != DUMMY_NODE) {
                node.trackDependency(oldNode);
            }
        }
    }

    /**
     * This method pushes given node to top of stack, and marks current on-stack node as dependant of given node.
     * This method returns current stack top; later it should be passed as an argument to
     * {@link DependencyTracker#exit(DependencyNode)}.
     * @param node node to push
     * @return previous on-stack dependency node
     */
    public static DependencyNode track(DependencyNode node) {
        DependencyNode oldNode = NODE.get();
        assert oldNode == null || node != null: "Could not reassign node to null";
        if (node != DUMMY_NODE) {
            if (oldNode != null && oldNode != DUMMY_NODE) {
                // отслеживаем только для существенных узлов
                node.trackDependency(oldNode);
            }
            NODE.set(node);
        } else if (oldNode == null) {
            // если этот узел первый, то все-таки выставим наш узел-индикатор
            NODE.set(node);
        }
        return oldNode;
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
            ((MxResourceImpl)resource).trackDependency(node);
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

    private static class DummyDependencyNode implements DependencyNode {
        private Reference<DependencyNode> selfReference = new HashWeakReference<DependencyNode>(this);

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
            // ничего не делаем, кэшу ничего не обязательно знать, что его узел ущербный
        }

        @Override
        public String toString() {
            return "<DUMMY>";
        }

        @Override
        public Reference<DependencyNode> getSelfReference() {
            return selfReference;
        }
    }

}
