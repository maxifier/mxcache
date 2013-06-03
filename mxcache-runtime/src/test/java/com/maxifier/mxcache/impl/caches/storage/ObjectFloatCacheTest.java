package com.maxifier.mxcache.impl.caches.storage;

import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.impl.wrapping.Wrapping;
import com.maxifier.mxcache.storage.ObjectFloatStorage;
import com.maxifier.mxcache.storage.elementlocked.ObjectFloatElementLockedStorage;
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
public class ObjectFloatCacheTest {
    private static final Signature SIGNATURE = new Signature(Object.class, float.class);

    private static final ObjectFloatCalculatable CALCULATABLE = new ObjectFloatCalculatable() {
        @Override
        public float calculate(Object owner, Object o) {
            assert o == "123";
            return 42f;
        }
    };

    @DataProvider(name = "both")
    public Object[][] v200v210v219() {
        return new Object[][] {{false}, {true}};
    }

    private static class Occupied implements ObjectFloatCalculatable {
        private boolean occupied;

        private int occupiedRequests;

        public Occupied() {

        }

        public synchronized void setOccupied(boolean occupied) {
            this.occupied = occupied;
            notifyAll();
        }

        @Override
        public synchronized float calculate(Object owner, Object o) {
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
            return 42f;
        }
    }

    @Test(dataProvider = "both")
    public void testOccupied(boolean elementLocked) throws Throwable {
        ObjectFloatStorage storage = createStorage(elementLocked);
        Occupied occupied = new Occupied();

        when(storage.isCalculated("123")).thenReturn(false);
        when(storage.size()).thenReturn(0);

        final ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
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

                    assert cache.getOrCreate("123") == 42f;

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
        verify(storage, atLeast(1)).isCalculated("123");
        verify(storage).save("123", 42f);
        if (elementLocked) {
            
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testMiss(boolean elementLocked) {
        ObjectFloatStorage storage = createStorage(elementLocked);

        when(storage.isCalculated("123")).thenReturn(false);
        when(storage.size()).thenReturn(0);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 0;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated("123");
        verify(storage).save("123", 42f);
        if (elementLocked) {
            
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testHit(boolean elementLocked) {
        ObjectFloatStorage storage = createStorage(elementLocked);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(storage.isCalculated("123")).thenReturn(true);
        when(storage.load("123")).thenReturn(42f);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        verify(storage, atLeast(1)).isCalculated("123");
        verify(storage).load("123");
        if (elementLocked) {
            
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testClear(boolean elementLocked) {
        ObjectFloatStorage storage = createStorage(elementLocked);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.clear();

        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "both")
    public void testSetDuringDependencyNodeOperations(boolean elementLocked) {
        ObjectFloatStorage storage = createStorage(elementLocked);

        when(storage.isCalculated("123")).thenReturn(false, true);
        when(storage.load("123")).thenReturn(42f);

        ObjectFloatCalculatable calculatable = mock(ObjectFloatCalculatable.class);
        MxResource r = mock(MxResource.class);
        when(calculatable.calculate("123", "123")).thenThrow(new ResourceOccupied(r));

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", calculatable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, times(2)).isCalculated("123");
        verify(storage).load("123");
        if (elementLocked) {
            
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
        verify(calculatable).calculate("123", "123");
        verifyNoMoreInteractions(calculatable);
    }

    @Test(dataProvider = "both")
    public void testResetStat(boolean elementLocked) {
        ObjectFloatStorage storage = createStorage(elementLocked);

        when(storage.isCalculated("123")).thenReturn(false);

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assert cache.getOrCreate("123") == 42f;

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage, atLeast(1)).isCalculated("123");
        verify(storage).save("123", 42f);
        if (elementLocked) {
            
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).lock("123");
                ((ObjectFloatElementLockedStorage)verify(storage, atLeast(1))).unlock("123");
            
        }
        verifyNoMoreInteractions(storage);
    }

    private ObjectFloatStorage createStorage(boolean elementLocked) {
        ObjectFloatStorage storage = mock(elementLocked ? ObjectFloatElementLockedStorage.class : ObjectFloatStorage.class);
        if (elementLocked) {
            when(((ObjectFloatElementLockedStorage)storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }    

    @Test(dataProvider = "both")
    public void testTransparentStat(boolean elementLocked) {
        ObjectFloatStorage storage = mock(elementLocked ? ObjectFloatElementLockedStorage.class : ObjectFloatStorage.class, withSettings().extraInterfaces(StatisticsHolder.class));

        ObjectFloatCache cache = (ObjectFloatCache) Wrapping.getFactory(SIGNATURE, SIGNATURE, elementLocked).
                wrap("123", CALCULATABLE, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder)storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }
}
