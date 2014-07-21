/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.BooleanBooleanStorage;
import com.maxifier.mxcache.storage.elementlocked.BooleanBooleanElementLockedStorage;
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
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 *
 * GENERATED FROM P2PCacheTest.template
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({ "unchecked" })
@Test
public class BooleanBooleanCacheTest {
    private static final Signature SIGNATURE = new Signature(boolean.class, boolean.class);

    private static final BooleanBooleanCalculatable CALCULATABLE = new BooleanBooleanCalculatable() {
        @Override
        public boolean calculate(Object owner, boolean o) {
            assert o == true;
            return true;
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    private static class Occupied implements BooleanBooleanCalculatable {
        private boolean occupied;

        private int occupiedRequests;

        public Occupied() {

        }

        public synchronized void setOccupied(boolean occupied) {
            this.occupied = occupied;
            notifyAll();
        }

        @Override
        public synchronized boolean calculate(Object owner, boolean o) {
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
            return true;
        }
    }

    @Test(dataProvider = "both")
    public void testOccupied(boolean elementLocked) throws Throwable {
        BooleanBooleanStorage storage = createStorage(elementLocked);
        Occupied occupied = new Occupied();

        when(storage.isCalculated(true)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        final BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
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

                    assert cache.getOrCreate(true) == true;

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
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, true);
        if (elementLocked) {
            
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        BooleanBooleanStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(true)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, true);
        if (elementLocked) {
            
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        BooleanBooleanStorage storage = createStorage(elementLocked);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated(true)).thenReturn(true);
        when(storage.load(true)).thenReturn(true);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).load(true);
        if (elementLocked) {
            
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        BooleanBooleanStorage storage = createStorage(elementLocked);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        BooleanBooleanStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(true)).thenReturn(false, true);
        when(storage.load(true)).thenReturn(true);

        BooleanBooleanCalculatable calculatable = mock(BooleanBooleanCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", true)).thenThrow(new ResourceOccupied(r));

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated(true);
        verify(storage).load(true);
        if (elementLocked) {
            
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", true);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        BooleanBooleanStorage storage = createStorage(elementLocked);

        when(storage.isCalculated(true)).thenReturn(false);

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(true) == true;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated(true);
        verify(storage).save(true, true);
        if (elementLocked) {
            
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).lock(true);
                ((BooleanBooleanElementLockedStorage)verify(storage, atLeast(1))).unlock(true);
            
        }
        verifyNoMoreInteractions(storage);
    }

    private BooleanBooleanStorage createStorage(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        BooleanBooleanStorage storage = mock((Class<BooleanBooleanStorage>)(elementLocked ? BooleanBooleanElementLockedStorage.class : BooleanBooleanStorage.class));
        if (elementLocked) {
            when(((BooleanBooleanElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }    

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        BooleanBooleanStorage storage = mock((Class<BooleanBooleanStorage>)(elementLocked ? BooleanBooleanElementLockedStorage.class : BooleanBooleanStorage.class), withSettings().extraInterfaces(StatisticsHolder.class));

        BooleanBooleanCache cache = (BooleanBooleanCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
