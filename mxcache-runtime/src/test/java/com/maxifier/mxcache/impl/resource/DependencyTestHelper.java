package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;
import gnu.trove.THashSet;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 17.06.11
 * Time: 11:58
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
        final Set<DependencyNode> nodes = new THashSet<DependencyNode>();
        final Queue<DependencyNode> queue = new LinkedList<DependencyNode>();

        DependencyNodeVisitor visitor = new CollectingDependencyNodeVisitor(nodes, queue);

        // we enqueue this but we don't add it cause we don't want to call it's appendElements(Set)
        node.visitDependantNodes(visitor);
        while (!queue.isEmpty()) {
            queue.poll().visitDependantNodes(visitor);
        }
        return nodes;
    }
}
