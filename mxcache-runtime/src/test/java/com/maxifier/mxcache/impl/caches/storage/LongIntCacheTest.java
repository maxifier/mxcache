package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.LongIntStorage;
import com.maxifier.mxcache.storage.elementlocked.LongIntElementLockedStorage;
import com.maxifier.mxcache.provider.Signature;
import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.impl.MutableStatisticsImpl;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import com.maxifier.mxcache.impl.resource.ResourceOccupied;
import com.maxifier.mxcache.interfaces.StatisticsHolder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.*;

import java.util.concurrent.locks.*;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 15.02.2010
 * Time: 13:40:10
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * @author ELectronic ENgine
 */
@SuppressWarnings({ "unchecked" })
@Test
public class LongIntCacheTest {
    private static final Signature SIGNATURE = new Signature(long.class, int.class);

    private static final LongIntCalculatable CALCULATABLE = new LongIntCalculatable() {
        @Override
        public int calculate(Object owner, long o) {
            assert o == 42L;
            return 42;
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    private static class Occupied implements LongIntCalculatable {
        private boolean occupied;

        private int occupiedRequests;

        public Occupied() {

        }

        public synchronized void setOccupied(boolean occupied) {
            this.occupied = occupied;
            notifyAll();
        }

        @Override
        public synchronized int calculate(Object owner, long o) {
            if (occupied) {
                occupiedRequests++;
                notifyAll();

                MxResource r = mock(MxResource.class);
                doAnswer(new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        synchronized(Occupied.this) {
                            while(occupied) {
                                Occupied.this.wait();
                            }
                        }
                        return null;
                    }
                }).when(r).waitForEndOfModification();
                throw new ResourceOccupied(r);
            }
            return 42;
        }
    }

    @Test(dataProvider = "both")
    public void testOccupied(boolean elementLocked) throws Throwable {
        LongIntStorage storage = createStorage(elementLocked);
        Occupied occupied = new Occupied();

        when(storage.isCalculated(42L)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        final LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", occupied, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        occupied.setOccupied(true);

        class TestThread extends Thread {
            public Throwable t;

            @Override
            public void run() {
                try {
                    assert cache.getSize() == 0;
                    assert cache.getStatistics().getHits() == 0;
                    assert cache.getStatistics().getMisses() == 0;

                    assert cache.getOrCreate(42L) == 42;

                    assertEquals(cache.getStatistics().getHits(), 0);
                    assertEquals(cache.getStatistics().getMisses(), 1);
                } catch (Throwable t) {
                    this.t = t;
                }
            }
        }
        TestThread t = new TestThread();
        t.start();

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized(occupied) {
            while(occupied.occupiedRequests == 0) {
                occupied.wait();
            }
        }

        occupied.setOccupied(false);

        t.join();

        if (t.t != null) {
            throw t.t;
        }

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).save(42L, 42);
        if (elementLocked) {
            
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        LongIntStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(42L)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).save(42L, 42);
        if (elementLocked) {
            
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        LongIntStorage storage = createStorage(elementLocked);

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated(42L)).thenReturn(true);
        when(storage.load(42L)).thenReturn(42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).load(42L);
        if (elementLocked) {
            
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        LongIntStorage storage = createStorage(elementLocked);

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        LongIntStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(42L)).thenReturn(false, true);
        when(storage.load(42L)).thenReturn(42);

        LongIntCalculatable calculatable = mock(LongIntCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42L)).thenThrow(new ResourceOccupied(r));

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(42L);
        verify(storage).load(42L);
        if (elementLocked) {
            
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42L);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        LongIntStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(42L)).thenReturn(false);

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42L) == 42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(42L);
        verify(storage).save(42L, 42);
        if (elementLocked) {
            
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).lock(42L);
                ((LongIntElementLockedStorage)verify(storage, atLeast(1))).unlock(42L);
            
        }
        verifyNoMoreInteractions(storage);
    }

    private LongIntStorage createStorage(boolean elementLocked) {
        LongIntStorage storage = mock(elementLocked ? LongIntElementLockedStorage.class : LongIntStorage.class);
        if (elementLocked) {
            when(((LongIntElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }    

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        LongIntStorage storage = mock(elementLocked ? LongIntElementLockedStorage.class : LongIntStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        LongIntCache cache = (LongIntCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
