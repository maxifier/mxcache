/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.locks.Lock;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({"UnusedAssignment"})
@Test
public class DependencyNodeUTest {
    public void testMultipleNodeToString() throws InterruptedException {
        DependencyNode node = new MultipleDependencyNode();

        CleaningNode n1 = new TestCleaningNode("Node1");
        CleaningNode n2 = new TestCleaningNode("Node2");

        node.addNode(n1);
        node.addNode(n2);
        String s = node.toString();
        // order doesn't matter
        Assert.assertTrue(s.equals("DependencyNode<Node1, Node2>") || s.equals("DependencyNode<Node2, Node1>"));

        TIdentityHashSet<CleaningNode> set = new TIdentityHashSet<CleaningNode>();
        node.appendNodes(set);

        Assert.assertEqualsNoOrder(set.toArray(), new Object[]{n1, n2});
        set.clear();

        n2 = null;
        bigGc();

        Assert.assertEquals(node.toString(), "DependencyNode<Node1, 1 GCed>");

        n1 = null;
        bigGc();

        Assert.assertEquals(node.toString(), "DependencyNode<2 GCed>");

        node.appendNodes(set);
        Assert.assertTrue(set.isEmpty());
    }

    public void testSingletonNodeToString() throws InterruptedException {
        DependencyNode node = new SingletonDependencyNode();

        CleaningNode n1 = new TestCleaningNode("Node1");

        node.addNode(n1);
        Assert.assertEquals(node.toString(), "DependencyNode<Node1>");
    }

    private static void bigGc() throws InterruptedException {
        System.gc();
        Thread.sleep(100);
        System.gc();
        Thread.sleep(100);
        System.gc();
    }

    private static class TestCleaningNode implements CleaningNode {
        private final String name;

        public TestCleaningNode(String name) {
            this.name = name;
        }

        @Override
        public Lock getLock() {
            return null;
        }

        @Override
        public void clear() {
        }

        @Override
        public DependencyNode getDependencyNode() {
            return null;
        }

        @Override
        public Object getCacheOwner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
