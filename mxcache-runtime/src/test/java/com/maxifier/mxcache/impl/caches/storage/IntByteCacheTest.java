/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.*;
import com.maxifier.mxcache.storage.elementlocked.*;
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
public class IntByteCacheTest {
    private static final Signature SIGNATURE = new Signature(int.class, byte.class);

    private static final IntByteCalculatable CALCULATABLE = new IntByteCalculatable() {
        @Override
        public byte calculate(Object owner, int o) {
            assert o == 42;
            return (byte)42;
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    private static class Occupied implements IntByteCalculatable {
        private boolean occupied;

        private int occupiedRequests;

        public Occupied() {

        }

        public synchronized void setOccupied(boolean occupied) {
            this.occupied = occupied;
            notifyAll();
        }

        @Override
        public synchronized byte calculate(Object owner, int o) {
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
            return (byte)42;
        }
    }

    @Test(dataProvider = "both", timeOut = 60000 /*ms*/)
    public void testOccupied(boolean elementLocked) throws Throwable {
        IntObjectStorage storage = createStorage(elementLocked);
        Occupied occupied = new Occupied();

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        final IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
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

                    assert cache.getOrCreate(42) == (byte)42;

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
        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, (byte)42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, (byte)42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.load(42)).thenReturn((byte)42);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).load(42);
        verify(storage).load(42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED, (byte)42);

        IntByteCalculatable calculatable = mock(IntByteCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", 42)).thenThrow(new ResourceOccupied(r));

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == (byte)42;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).load(42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", 42);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        IntObjectStorage storage = createStorage(elementLocked);

        when(storage.load(42)).thenReturn(Storage.UNDEFINED);

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate(42) == (byte)42;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).load(42);
        verify(storage).save(42, (byte)42);
        if (elementLocked) {
            
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).lock(42);
                ((IntObjectElementLockedStorage)verify(storage, atLeast(1))).unlock(42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    private IntObjectStorage createStorage(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        IntObjectStorage storage = mock((Class<IntObjectStorage>)(elementLocked ? IntObjectElementLockedStorage.class : IntObjectStorage.class));
        if (elementLocked) {
            when(((IntObjectElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }    

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        // cast necessary for JDK8 compilation
        IntObjectStorage storage = mock((Class<IntObjectStorage>)(elementLocked ? IntObjectElementLockedStorage.class : IntObjectStorage.class), withSettings().extraInterfaces(StatisticsHolder.class));

        IntByteCache cache = (IntByteCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
