/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource.nodes;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({"UnusedAssignment"})
@Test
public class DependencyNodeUTest {
    public void testNoDirectReferencesInMultipleNode() throws InterruptedException {
        DependencyNode node = new MultipleDependencyNode();

        CleaningNode n1 = mock(CleaningNode.class);
        CleaningNode n2 = mock(CleaningNode.class);
        when(n1.toString()).thenReturn("Node1");
        when(n2.toString()).thenReturn("Node2");

        Reference<CleaningNode> n1ref = new WeakReference<CleaningNode>(n1);
        Reference<CleaningNode> n2ref = new WeakReference<CleaningNode>(n2);

        node.addNode(n1);
        node.addNode(n2);
        String s = node.toString();
        // order doesn't matter
        Assert.assertTrue(s.equals("DependencyNode<Node1, Node2>") || s.equals("DependencyNode<Node2, Node1>"));

        node.invalidate();

        verify(n1).invalidate();
        verify(n2).invalidate();

        n2 = null;
        bigGc();

        assertNull(n2ref.get());

        node.invalidate();
        verify(n1).invalidate();

        n1 = null;
        bigGc();

        assertNull(n1ref.get());

        // no NPE here, no exceptions...
        node.invalidate();
    }

    public void testSingletonNodeToString() throws InterruptedException {
        DependencyNode node = new SingletonDependencyNode();

        CleaningNode n1 = mock(CleaningNode.class);

        node.addNode(n1);

        node.invalidate();
        verify(n1).invalidate();
    }

    // yep, there's no guarantee, but we can try...
    private static void bigGc() throws InterruptedException {
        System.gc();
        Thread.sleep(100);
        System.gc();
        Thread.sleep(100);
        System.gc();
    }
}
