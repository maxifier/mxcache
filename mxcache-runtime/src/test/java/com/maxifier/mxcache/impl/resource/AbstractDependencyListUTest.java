package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.lang.ref.WeakReference;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.04.2010
 * Time: 11:02:10
 */
@Test
public class AbstractDependencyListUTest {
    // да ничо особо тут не проверяется - так, на всякий случай
    public void testSelfDependenct() {
        TestNode a = new TestNode("A");
        a.trackDependency(a);
        assert a.getApproxSize() == 1;
        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(a);
        assert nodes.size() == 1;
        assert nodes.contains(a);
    }

    public void testGCnode() throws Exception {
        TestNode a = new TestNode("A");
        TestNode b = new TestNode("B");
        TestNode c = new TestNode("C");
        TestNode d = new TestNode("D");

        a.trackDependency(b);
        a.trackDependency(d);
        b.trackDependency(c);

        // тут примерные размер известен точно, потому что все элементы живы
        assert a.getApproxSize() == 2;
        assert b.getApproxSize() == 1;
        assert c.getApproxSize() == 0;

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(a);
        assert nodes.size() == 3;
        assert nodes.contains(b);
        assert nodes.contains(c);
        assert nodes.contains(d);

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
        assert a.getApproxSize() <= 2;

        Set<DependencyNode> nodes2 = DependencyTestHelper.getAllDependentNodes(a);
        // теперь все связи через b разорваны.
        assert nodes2.size() == 1;
        assert nodes2.contains(d);
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

        Set<DependencyNode> nodes = DependencyTestHelper.getAllDependentNodes(a);

        Assert.assertEquals(nodes.size(), 5);
        // сам узел тоже попадает, потому что зависит от D
        Assert.assertTrue(nodes.contains(a));
        Assert.assertTrue(nodes.contains(b));
        Assert.assertTrue(nodes.contains(c));
        Assert.assertTrue(nodes.contains(d));
        Assert.assertTrue(nodes.contains(e));

        Set<DependencyNode> nodes2 = DependencyTestHelper.getAllDependentNodes(d);

        Assert.assertEquals(nodes2.size(), 5);
        Assert.assertTrue(nodes2.contains(a));
        Assert.assertTrue(nodes2.contains(b));
        Assert.assertTrue(nodes2.contains(c));
        Assert.assertTrue(nodes2.contains(d));
        Assert.assertTrue(nodes2.contains(e));

        Set<DependencyNode> nodes3 = DependencyTestHelper.getAllDependentNodes(c);

        Assert.assertEquals(nodes3.size(), 1);
        // а сам узел C не попал.
        Assert.assertTrue(nodes3.contains(e));
    }

    private static class TestNode extends AbstractDependencyNode implements DependencyNode {
        private final String name;

        TestNode(String name) {
            this.name = name;
        }

        @Override
        public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addNode(@NotNull CleaningNode cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
