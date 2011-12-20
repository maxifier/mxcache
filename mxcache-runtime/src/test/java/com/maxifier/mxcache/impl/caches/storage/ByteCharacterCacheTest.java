package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.storage.ByteCharacterStorage;
import com.maxifier.mxcache.storage.elementlocked.ByteCharacterElementLockedStorage;
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
public class ByteCharacterCacheTest {
    private static final Signature SIGNATURE = new Signature(byte.class, char.class);

    private static final ByteCharacterCalculatable CALCULATABLE = new ByteCharacterCalculatable() {
        @Override
        public char calculate(Object owner, byte o) {
            assert o == (byte)42;
            return '*';
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    private static class Occupied implements ByteCharacterCalculatable {
        private boolean occupied;

        private int occupiedRequests;

        public Occupied() {

        }

        public synchronized void setOccupied(boolean occupied) {
            this.occupied = occupied;
            notifyAll();
        }

        @Override
        public synchronized char calculate(Object owner, byte o) {
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
            return '*';
        }
    }

    @Test(dataProvider = "both")
    public void testOccupied(boolean elementLocked) throws Throwable {
        ByteCharacterStorage storage = createStorage(elementLocked);
        Occupied occupied = new Occupied();

        when(storage.isCalculated((byte)42)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        final ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
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

                    assert cache.getOrCreate((byte)42) == '*';

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
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, '*');
        if (elementLocked) {
            
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        ByteCharacterStorage storage = createStorage(elementLocked);

        when(storage.isCalculated((byte)42)).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, '*');
        if (elementLocked) {
            
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        ByteCharacterStorage storage = createStorage(elementLocked);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated((byte)42)).thenReturn(true);
        when(storage.load((byte)42)).thenReturn('*');
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        if (elementLocked) {
            
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        ByteCharacterStorage storage = createStorage(elementLocked);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        ByteCharacterStorage storage = createStorage(elementLocked);

        when(storage.isCalculated((byte)42)).thenReturn(false, true);
        when(storage.load((byte)42)).thenReturn('*');

        ByteCharacterCalculatable calculatable = mock(ByteCharacterCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", (byte)42)).thenThrow(new ResourceOccupied(r));

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated((byte)42);
        verify(storage).load((byte)42);
        if (elementLocked) {
            
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", (byte)42);
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        ByteCharacterStorage storage = createStorage(elementLocked);

        when(storage.isCalculated((byte)42)).thenReturn(false);

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate((byte)42) == '*';

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated((byte)42);
        verify(storage).save((byte)42, '*');
        if (elementLocked) {
            
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).lock((byte)42);
                ((ByteCharacterElementLockedStorage)verify(storage, atLeast(1))).unlock((byte)42);
            
        }
        verifyNoMoreInteractions(storage);
    }

    private ByteCharacterStorage createStorage(boolean elementLocked) {
        ByteCharacterStorage storage = mock(elementLocked ? ByteCharacterElementLockedStorage.class : ByteCharacterStorage.class);
        if (elementLocked) {
            when(((ByteCharacterElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }    

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        ByteCharacterStorage storage = mock(elementLocked ? ByteCharacterElementLockedStorage.class : ByteCharacterStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        ByteCharacterCache cache = (ByteCharacterCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
