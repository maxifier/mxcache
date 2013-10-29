package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.AbstractDependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyNodeVisitor;
import com.maxifier.mxcache.impl.resource.MxStaticResource;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import com.maxifier.mxcache.util.TIdentityHashSet;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

import java.lang.ref.Reference;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 17.06.11
 * Time: 13:12
 */
@SuppressWarnings({"unchecked"})
@Test
public class CleaningHelperUTest {
    public void testLockAndClear() {
        CleaningNode n1 = mock(CleaningNode.class);
        final CleaningNode n2 = mock(CleaningNode.class);

        final CleaningNode n3 = mock(CleaningNode.class);

        ReentrantLock lock1 = spy(new ReentrantLock());
        ReentrantLock lock2 = spy(new ReentrantLock());
        ReentrantLock lock3 = spy(new ReentrantLock());

        when(n1.getLock()).thenReturn(lock1);
        when(n2.getLock()).thenReturn(lock2);
        when(n3.getLock()).thenReturn(lock3);

        DependencyNode dn1 = new SingletonDependencyNode();
        DependencyNode dn2 = new SingletonDependencyNode();
        DependencyNode dn3 = new SingletonDependencyNode();

        dn1.addNode(n1);
        dn2.addNode(n2);
        dn3.addNode(n3);

        dn1.trackDependency(dn3);

        when(n1.getDependencyNode()).thenReturn(dn1);
        when(n2.getDependencyNode()).thenReturn(dn2);
        when(n3.getDependencyNode()).thenReturn(dn3);

        CleaningHelper.lockAndClear(Arrays.asList(n1, n2));

        verify(lock1, atLeast(1)).tryLock();
        verify(lock1, atLeast(1)).unlock();
        verify(lock2, atLeast(1)).tryLock();
        verify(lock2, atLeast(1)).unlock();
        verify(lock3, atLeast(1)).tryLock();
        verify(lock3, atLeast(1)).unlock();

        verify(n1).clear();
        verify(n2).clear();
        verify(n3).clear();

        verify(n1, atLeast(1)).getDependencyNode();
        verify(n2, atLeast(1)).getDependencyNode();
    }

    public void testAlreadyLocked() {
        Lock lock = new ReentrantLock();
        lock.lock();
        try {
            CleaningHelper.lock(Arrays.asList(lock));
            CleaningHelper.unlock(Arrays.asList(lock));
        } finally {
            lock.unlock();
        }
    }

    @Test(timeOut = 60000)
    public void testLocking() {
        final CleaningNode cn1 = mock(CleaningNode.class);
        when(cn1.getLock()).thenReturn(new ReentrantLock());

        final AbstractDependencyNode n1 = new AbstractDependencyNode() {
            @Override
            public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
                elements.add(cn1);
            }

            @Override
            public void addNode(@NotNull CleaningNode cache) {
                throw new UnsupportedOperationException();
            }
        };
        when(cn1.getDependencyNode()).thenReturn(n1);

        final CleaningNode cn2 = mock(CleaningNode.class);
        when(cn2.getLock()).thenReturn(new ReentrantLock());
        AbstractDependencyNode n2 = new AbstractDependencyNode() {
            @Override
            public void appendNodes(TIdentityHashSet<CleaningNode> elements) {
                Thread t = new Thread() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public void run() {
                        Lock l1 = cn1.getLock();
                        l1.lock();
                        try {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ignored) {
                            }

                            Lock l2 = cn2.getLock();
                            l2.lock();
                            try {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException ignored) {
                                }
                            } finally {
                                l2.unlock();
                            }
                        } finally {
                            l1.unlock();
                        }
                    }
                };
                t.start();
                try {
                    t.join(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                elements.add(cn2);
                trackDependency(n1);
            }

            @Override
            public void addNode(@NotNull CleaningNode cache) {
                throw new UnsupportedOperationException();
            }
        };
        when(cn2.getDependencyNode()).thenReturn(n2);

        MxStaticResource r = new MxStaticResource("test");
        r.trackDependency(n2);

        TIdentityHashSet<CleaningNode> s = CleaningHelper.lockRecursive(r);
        System.out.println(s);
    }
}
