package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.util.HashWeakReference;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import org.jetbrains.annotations.NotNull;

import java.lang.ref.Reference;

import static com.maxifier.mxcache.impl.resource.DependencyTracker.DUMMY_NODE;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 12:28:44
 */
@Test
public class DependencyTrackerUTest {
    public void simpleTest() {
        TestDependencyNode a = new TestDependencyNode(null);
        TestDependencyNode b = new TestDependencyNode(a);
        TestDependencyNode c = new TestDependencyNode(b);
        TestDependencyNode d = new TestDependencyNode(c);
        // DUMMY_NODE сохраняется в стене, только если он первый. Иначе стек будет неизменным. 
        assertEquals(DependencyTracker.track(DUMMY_NODE), null);
        assertTrue(DependencyTracker.hasUnderlyingNode());
        assertEquals(DependencyTracker.track(a), DUMMY_NODE);
        assertEquals(DependencyTracker.track(DUMMY_NODE), a);
        assertEquals(DependencyTracker.track(b), a);
        assertEquals(DependencyTracker.track(DUMMY_NODE), b);
        assertEquals(DependencyTracker.track(c), b);
        assertEquals(DependencyTracker.track(DUMMY_NODE), c);
        assertEquals(DependencyTracker.track(d), c);
        assertTrue(DependencyTracker.hasUnderlyingNode());
        assertEquals(DependencyTracker.get(), d);
        DependencyTracker.exit(c);
        assertEquals(DependencyTracker.get(), c);
        DependencyTracker.exit(c);
        assertEquals(DependencyTracker.get(), c);
        DependencyTracker.exit(b);
        assertEquals(DependencyTracker.get(), b);
        DependencyTracker.exit(b);
        assertEquals(DependencyTracker.get(), b);
        DependencyTracker.exit(a);
        assertEquals(DependencyTracker.get(), a);
        DependencyTracker.exit(a);
        assertEquals(DependencyTracker.get(), a);
        DependencyTracker.exit(DUMMY_NODE);
        assertTrue(DependencyTracker.hasUnderlyingNode());
        assertEquals(DependencyTracker.get(), DUMMY_NODE);
        DependencyTracker.exit(null);
        assertTrue(!DependencyTracker.hasUnderlyingNode());

        a.check();
        b.check();
        c.check();
        d.check();
    }

    private static class TestDependencyNode implements DependencyNode {
        private final DependencyNode expectedNode;
        private boolean ok;

        private TestDependencyNode(DependencyNode expectedNode) {
            this.expectedNode = expectedNode;
        }

        @Override
        public Reference<DependencyNode> getSelfReference() {
            return new HashWeakReference<DependencyNode>(this);
        }

        @Override
        public void visitDependantNodes(DependencyNodeVisitor visitor) {
            fail("Should not reach here");
        }

        @Override
        public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
            fail("Should not reach here");
        }

        @Override
        public void addNode(@NotNull CleaningNode cache) {
            fail("Should not reach here");
        }

        @Override
        public void trackDependency(DependencyNode node) {
            ok = true;
            assert expectedNode != null;
            assertEquals(node, expectedNode);
        }

        public void check() {
            assert expectedNode == null || ok;
        }
    }

    public void testDummyEnqueueDependentNodes() {
        DependencyNodeVisitor mock = mock(DependencyNodeVisitor.class);
        DUMMY_NODE.visitDependantNodes(mock);
        verifyZeroInteractions(mock);

    }

    @Test (expectedExceptions = UnsupportedOperationException.class)
    public void testDummyAppendElements() {
        DUMMY_NODE.appendNodes(new TIdentityHashSet<CleaningNode>());
    }

    @Test (expectedExceptions = UnsupportedOperationException.class)
    public void testDummyTrackDependency() {
        DUMMY_NODE.trackDependency(DUMMY_NODE);
    }
}
