package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.nodes.SingletonDependencyNode;
import org.testng.annotations.Test;

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
}
