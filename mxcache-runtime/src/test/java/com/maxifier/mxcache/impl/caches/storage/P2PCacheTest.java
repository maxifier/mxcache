/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
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

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.*;

import static com.maxifier.mxcache.impl.caches.storage.TestHelper.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static org.testng.Assert.*;

/**
 * THIS IS GENERATED CLASS! DON'T EDIT IT MANUALLY!
 * <p/>
 * GENERATED FROM #SOURCE#
 *
 * @author Andrey Yakoushin (andrey.yakoushin@maxifier.com)
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings({"unchecked"})
@Test
public class P2PCacheTest {
    private static final Object[][] PRIMITIVES = {
            {boolean.class, true},
            {byte.class, (byte) 42},
            {short.class, (short) 43},
            {char.class, '*'},
            {int.class, 44},
            {long.class, 45L},
            {float.class, 46.5f},
            {double.class, 47.5},
            {Object.class, "123"},
            // test null especially
            {Object.class, null}
    };

    @DataProvider
    public Object[][] getAllTypes() {
        Object[][] res = new Object[PRIMITIVES.length * (PRIMITIVES.length + 1) * 2][];
        for (int i = 0; i < PRIMITIVES.length + 1; i++) {
            for (int j = 0; j < PRIMITIVES.length; j++) {
                Object keyType;
                Object key;
                if (i == PRIMITIVES.length) {
                    // null key = cache without a key (lazy initialization)
                    keyType = null;
                    key = null;
                } else {
                    keyType = PRIMITIVES[i][0];
                    key = PRIMITIVES[i][1];
                }
                res[(i * PRIMITIVES.length + j) * 2] = new Object[]{keyType, PRIMITIVES[j][0], key, PRIMITIVES[j][1], false};
                res[(i * PRIMITIVES.length + j) * 2 + 1] = new Object[]{keyType, PRIMITIVES[j][0], key, PRIMITIVES[j][1], true};
            }
        }
        return res;
    }

    @Test(dataProvider = "getAllTypes", timeOut = 60000 /*ms*/)
    public <K, V> void testOccupied(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);

        when(load(storage, key)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(0);

        final AtomicBoolean occupied = new AtomicBoolean();
        final AtomicInteger occupiedRequests = new AtomicInteger();
        final Object lock = new Object();

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                synchronized (lock) {
                    if (occupied.get()) {
                        occupiedRequests.incrementAndGet();
                        lock.notifyAll();

                        MxResource r = mock(MxResource.class);
                        doAnswer(new Answer() {
                            @Override
                            public Object answer(InvocationOnMock invocation) throws Throwable {
                                synchronized (lock) {
                                    while (occupied.get()) {
                                        lock.wait();
                                    }
                                }
                                return null;
                            }
                        }).when(r).waitForEndOfModification();
                        throw new ResourceOccupied(r);
                    }
                }
                return value;
            }
        });

        final Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        occupied.set(true);

        class TestThread extends Thread {
            public Throwable t;

            @Override
            public void run() {
                try {
                    assert cache.getSize() == 0;
                    assert cache.getStatistics().getHits() == 0;
                    assert cache.getStatistics().getMisses() == 0;

                    assertEquals(getOrCreate(cache, key), value);

                    assertEquals(cache.getStatistics().getHits(), 0);
                } catch (Throwable t) {
                    this.t = t;
                }
            }
        }
        TestThread t = new TestThread();
        t.start();

        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (lock) {
            while (occupiedRequests.get() == 0) {
                lock.wait();
            }
            occupied.set(false);
            lock.notifyAll();
        }

        t.join();

        if (t.t != null) {
            throw t.t;
        }

        verify(storage).size();
        load(verify(storage, atLeast(1)), key);
        save(verify(storage), key, value);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
                unlock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testMiss(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);

        when(load(storage, key)).thenReturn(Storage.UNDEFINED);
        when(storage.size()).thenReturn(444);

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getSize() == 444;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(getOrCreate(cache, key), value);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        verify(storage).size();
        load(verify(storage, atLeast(1)), key);
        save(verify(storage), key, value);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
                unlock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testHit(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        when(load(storage, key)).thenReturn(value);
        when(storage.size()).thenReturn(1);

        assert cache.getSize() == 1;
        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(getOrCreate(cache, key), value);

        assert cache.getStatistics().getHits() == 1;
        assert cache.getStatistics().getMisses() == 0;

        verify(storage).size();
        load(verify(storage, atLeast(1)), key);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
                unlock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testClear(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.invalidate();
        if (elementLocked) {
            verify((ElementLockedStorage)storage).getLock();
        }
        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testSetDuringDependencyNodeOperations(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);

        when(load(storage, key)).thenReturn(Storage.UNDEFINED, value);

        Calculable calculable = mock(signature.getCalculableInterface());

        MxResource r = mock(MxResource.class);
        when(calculate(calculable, key)).thenThrow(new ResourceOccupied(r));

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(getOrCreate(cache, key), value);

        assertEquals(cache.getStatistics().getHits(), 1);
        // ResourceOccupied is considered a miss
        assertEquals(cache.getStatistics().getMisses(), 1);

        load(verify(storage, times(2)), key);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
                unlock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
        calculate(verify(calculable), key);
        verifyNoMoreInteractions(calculable);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testResetStat(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);
        Storage storage = createStorage(signature, elementLocked);
        when(load(storage, key)).thenReturn(Storage.UNDEFINED);

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        assertEquals(getOrCreate(cache, key), value);

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 1;

        cache.getStatistics().reset();

        assert cache.getStatistics().getHits() == 0;
        assert cache.getStatistics().getMisses() == 0;

        load(verify(storage, atLeast(1)), key);
        save(verify(storage), key, value);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
                unlock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
    }

    private Storage createStorage(Signature signature, boolean elementLocked) {
        // cast necessary for JDK8 compilation
        Storage storage = mock((Class<Storage>) (elementLocked ? signature.getElementLockedStorageInterface() : signature.getStorageInterface()));
        if (elementLocked) {
            when(((ElementLockedStorage) storage).getLock()).thenReturn(new ReentrantLock());
        }
        return storage;
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testTransparentStat(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws InvocationTargetException, IllegalAccessException {
        Signature signature = new Signature(keyType, valueType);

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenReturn(value);

        // cast necessary for JDK8 compilation
        Storage storage = mock((Class<Storage>) (elementLocked ? signature.getElementLockedStorageInterface() : signature.getStorageInterface()), withSettings().extraInterfaces(StatisticsHolder.class));

        Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        cache.getStatistics();

        verify((StatisticsHolder) storage).getStatistics();
        verifyNoMoreInteractions(storage);
    }

    @Test(dataProvider = "getAllTypes")
    public <K, V> void testCleanWhileCalculating(Class<K> keyType, Class<V> valueType, final K key, final V value, boolean elementLocked) throws Throwable {
        Signature signature = new Signature(keyType, valueType);
        final Sequence s = new Sequence();

        final ReentrantLock lock = new ReentrantLock();

        Calculable calculable = mock(signature.getCalculableInterface());
        when(calculate(calculable, key)).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                s.order(0);
                s.order(3);
                return value;
            }
        });

        // cast necessary for JDK8 compilation
        Storage storage = mock((Class<Storage>) (elementLocked ? signature.getElementLockedStorageInterface() : signature.getStorageInterface()), withSettings().extraInterfaces(StatisticsHolder.class));
        when(load(storage, key)).thenReturn(Storage.UNDEFINED);
        if (elementLocked) {
            setLock(key, signature, lock, storage);
        }


        final Cache cache = Wrapping.getFactory(signature, signature, elementLocked).
                wrap("123", calculable, storage, new MutableStatisticsImpl());
        cache.setDependencyNode(DependencyTracker.DUMMY_NODE);

        Thread t = new Thread("Test thread") {
            @Override
            public void run() {
                try {
                    assertEquals(getOrCreate(cache, key), value);
                } catch (Throwable e) {
                    s.fail(e);
                }
            }
        };
        t.start();
        s.order(1);

        load(verify(storage), key);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                lock(verify(storage, atLeast(1)), key);
            } else {
                ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            }
        }
        verifyNoMoreInteractions(storage);
        reset(storage);
        if (elementLocked) {
            setLock(key, signature, lock, storage);
        }

        // invalidate must be non-blocking
        cache.invalidate();


        if (elementLocked) {
            // the lock is acquired to set dirty flag
            ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
            verifyNoMoreInteractions(storage);
        } else {
            // storage should not be cleaned until the calculate is terminated and lock is released!
            verifyZeroInteractions(storage);
        }
        reset(storage);
        if (elementLocked) {
            setLock(key, signature, lock, storage);
        }

        s.order(2);

        t.join();
        s.check();

        save(verify(storage), key, value);
        if (elementLocked) {
            if (signature.getContainer() != null) {
                unlock(verify(storage, atLeast(1)), key);
            }
            // it is locked for cleaning
            ((ElementLockedStorage) verify(storage, atLeast(1))).getLock();
        }
        // it should be cleared immediately after  the unlock
        verify(storage).clear();
        verifyNoMoreInteractions(storage);
    }

    private <K> void setLock(K key, Signature signature, final ReentrantLock lock, Storage storage) throws InvocationTargetException, IllegalAccessException {
        when(((ElementLockedStorage) storage).getLock()).thenReturn(lock);
        if (signature.getContainer() != null) {
            lock(doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    lock.lock();
                    return null;
                }
            }).when(storage), key);
            unlock(doAnswer(new Answer<Object>() {
                @Override
                public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                    lock.unlock();
                    return null;
                }
            }).when(storage), key);
        }
    }
}
