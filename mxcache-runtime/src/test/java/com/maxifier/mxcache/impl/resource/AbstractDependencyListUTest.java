/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;
import gnu.trove.set.hash.THashSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Nonnull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.lang.ref.WeakReference;

import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class AbstractDependencyListUTest {
    /**
     * @param node node
     * @return список всех узлов, зависящих от данного, в том числе транзитивные зависимости. Сам узел включается в
     * этот список.
     */
    static Set<DependencyNode> getAllDependentNodes(DependencyNode node) {
        Set<DependencyNode> nodes = new THashSet<DependencyNode>();
        DependencyTracker.deepVisit(Collections.singleton(node), new CollectingDeepVisitor(nodes));
        return nodes;
    }

    public void testGCnode() throws Exception {
        TestNode a = new TestNode("A");
        TestNode b = new TestNode("B");
        TestNode c = new TestNode("C");
        TestNode d = new TestNode("D");

        a.trackDependency(b);
        a.trackDependency(d);
        b.trackDependency(c);

        //
        //  (A) <-- (B) <-- (C)
        //   ^
        //    \
        //     \
        //     (D)


        // тут примерные размер известен точно, потому что все элементы живы
        assertEquals(a.getApproxSize(), 2);
        assertEquals(b.getApproxSize(), 1);
        assertEquals(c.getApproxSize(), 0);

        Set<DependencyNode> nodes = getAllDependentNodes(a);
        assertEquals(nodes.size(), 4);
        assertTrue(nodes.contains(a));
        assertTrue(nodes.contains(b));
        assertTrue(nodes.contains(c));
        assertTrue(nodes.contains(d));

        // ссылка на b сохраняется в списке, поэтому его тоже надо удалить
        //noinspection UnusedAssignment,ReuseOfLocalVariable
        nodes = null;

        WeakReference<TestNode> r = new WeakReference<TestNode>(b);
        //noinspection UnusedAssignment,ReuseOfLocalVariable
        b = null;
        while (r.get() != null) {
            System.gc();
            Thread.sleep(10);
        }

        // тут мы можем только сказать, что новых узлов нет. старый мог и не быть удален
        assertTrue(a.getApproxSize() <= 2);

        Set<DependencyNode> nodes2 = getAllDependentNodes(a);
        // теперь все связи через b разорваны.
        assertEquals(nodes2.size(), 2);
        assertTrue(nodes2.contains(a));
        assertTrue(nodes2.contains(d));
    }

    public void testCyclicDependency() {
        TestNode a = new TestNode("A");
        TestNode b = new TestNode("B");
        TestNode c = new TestNode("C");
        TestNode d = new TestNode("D");
        TestNode e = new TestNode("E");

        a.trackDependency(b);
        b.trackDependency(c);
        b.trackDependency(d);
        d.trackDependency(a);
        c.trackDependency(e);

        //
        //  (A) <-- (B) <-- (C) <-- (E)
        //   \      ^
        //    \    /
        //     v  /
        //     (D)

        Set<DependencyNode> nodes = getAllDependentNodes(a);

        assertEquals(nodes.size(), 5);
        // сам узел тоже попадает, потому что зависит от D
        Assert.assertTrue(nodes.contains(a));
        Assert.assertTrue(nodes.contains(b));
        Assert.assertTrue(nodes.contains(c));
        Assert.assertTrue(nodes.contains(d));
        Assert.assertTrue(nodes.contains(e));

        Set<DependencyNode> nodes2 = getAllDependentNodes(d);

        assertEquals(nodes2.size(), 5);
        Assert.assertTrue(nodes2.contains(a));
        Assert.assertTrue(nodes2.contains(b));
        Assert.assertTrue(nodes2.contains(c));
        Assert.assertTrue(nodes2.contains(d));
        Assert.assertTrue(nodes2.contains(e));

        Set<DependencyNode> nodes3 = getAllDependentNodes(c);

        assertEquals(nodes3.size(), 2);
        Assert.assertTrue(nodes3.contains(c));
        Assert.assertTrue(nodes3.contains(e));
    }

    private static class TestNode extends AbstractDependencyNode implements DependencyNode {
        private final String name;

        TestNode(String name) {
            this.name = name;
        }

        @Override
        public void invalidate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addNode(@Nonnull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
     */
    static class CollectingDeepVisitor implements DependencyNode.Visitor {
        private final Set<DependencyNode> visitedNodes;
        private final Queue<DependencyNode> queue;

        public CollectingDeepVisitor(Set<DependencyNode> visitedNodes) {
            this.visitedNodes = visitedNodes;
            this.queue = new LinkedList<DependencyNode>();
        }

        @Override
        public void visit(DependencyNode node) {
            if (visitedNodes.add(node)) {
                queue.add(node);
            }
        }

        @Override
        public Queue<DependencyNode> getQueue() {
            return queue;
        }
    }
}
