package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;
import com.maxifier.mxcache.resource.ResourceModificationException;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 11:24:42
 */
@Test
public class MxResourceUTest {
    public void testRead() {
        MxResource r = MxResourceFactory.getResource("testResource");
        r.readStart();
        try {
            assert r.isReading();
        } finally {
            r.readEnd();
        }
    }

    public void testWrite() {
        MxResource r = MxResourceFactory.getResource("testResource");
        r.writeStart();
        try {
            assert r.isWriting();
        } finally {
            r.writeEnd();
        }
    }

    @Test(expectedExceptions = IllegalMonitorStateException.class)
    public void testWriteButEndRead() {
        // этот тест тестирует ReentrantReadWriteLock, но кто знает, может по€витс€ друга€ реализаци€
        MxResource r = MxResourceFactory.getResource("testResource");
        r.writeStart();
        try {
            assert r.isWriting();
            r.readEnd();
        } finally {
            r.writeEnd();
        }
    }

    public void testEquality() {
        MxResource r1 = MxResourceFactory.getResource("testResource");
        MxResource r2 = MxResourceFactory.getResource("testResource");
        // конечно лучше не сравнивать так ресурсы, однако раз MxResourceFactory гарантирует это,
        // то надо убедитьс€
        assert r1 == r2;

        // авось
        assert r1.equals(r2);

        // на вс€кий случай
        assert r1.getName().equals("testResource");

        // эти строки имеют одинаковый хэш, но это не важно
        MxResource r3 = MxResourceFactory.getResource("0-42L");
        MxResource r4 = MxResourceFactory.getResource("0-43-");
        assert !r3.equals(r4);
        assert !r4.equals(r3);
    }

    public void testSerialization() throws Exception {
        MxResource r1 = MxResourceFactory.getResource("testResource");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(r1);
        } finally {
            oos.close();
        }
        MxResource r2;
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            r2 = (MxResource) ois.readObject();
        } finally {
            ois.close();
        }
        // сериализаци€ не должна приводить к ложным дубликатам ресурсов
        assert r1 == r2;
    }

    @Test(expectedExceptions = ResourceModificationException.class)
    public void testWriteInCache() {
        MxResource r = MxResourceFactory.getResource("testResource");
        DependencyTracker.track(DependencyTracker.DUMMY_NODE);
        try {
            r.writeStart();
        } finally {
            DependencyTracker.exit(null);
        }
    }

    @Test (expectedExceptions = ResourceModificationException.class)
    public void testWaitForEndOfModificationFail() {
        MxResource r = MxResourceFactory.getResource("testResource");

        r.writeStart();
        try {
            r.waitForEndOfModification();
        } finally {
            r.writeEnd();
        }
    }

    public void testWaitForEndOfModification() {
        MxResource r = MxResourceFactory.getResource("testResource");
        // it shouldn't wait...
        r.waitForEndOfModification();
    }

    /**
     * «десь мы должны получить именно ResourceModificationException, поскольку сам кэш не может такую зависимость
     * разрешить: это означает, что при модификации ресурса произошло обращение к кэшу, завис€щему от этого самого
     * ресурса. 
     */
    @Test (expectedExceptions = ResourceModificationException.class)
    public void testReadInCacheWhileWriteFromSameThread() {
        MxResource r = MxResourceFactory.getResource("testResource");

        r.writeStart();
        try {
            DependencyTracker.track(DependencyTracker.DUMMY_NODE);
            try {
                r.readStart();
            } finally {
                DependencyTracker.exit(null);
            }
        } finally {
            r.writeEnd();
        }
    }

    /**
     * ј тут имеем чистой воды ResourceOccupied.
     * @throws InterruptedException ой! 
     */
    public void testReadInCacheWhileWriteFromOtherThread() throws InterruptedException {
        MxResource r = MxResourceFactory.getResource("testResource");
        Lock lock = new ReentrantLock();

        Condition startCondition = lock.newCondition();
        Condition endCondition = lock.newCondition();

        Thread t = new TesterThread(r, lock, startCondition, endCondition);
        t.start();

        lock.lock();
        try {
            // дождемс€, пока ресурс будет заблокирован записью
            startCondition.awaitUninterruptibly();
        } finally {
            lock.unlock();
        }

        DependencyTracker.track(DependencyTracker.DUMMY_NODE);
        try {
            try {
                r.readStart();
                Assert.fail("ResourceOccupied expected");
            } catch (ResourceOccupied e) {
                // ƒолжен быть установлен ресурс!
                assert e.getResource().equals(r);
            }
        } finally {
            DependencyTracker.exit(null);
            lock.lock();
            try {
                // все, можно запись завершить
                endCondition.signal();
            } finally {
                lock.unlock();
            }

            t.join();
        }
    }

    private static class TesterThread extends Thread {
        private final MxResource r;
        private final Lock lock;
        private final Condition startCondition;
        private final Condition endCondition;

        public TesterThread(MxResource r, Lock lock, Condition startCondition, Condition endCondition) {
            this.r = r;
            this.lock = lock;
            this.startCondition = startCondition;
            this.endCondition = endCondition;
        }

        @Override
                public void run() {
            r.writeStart();
            try {
                lock.lock();
                try {
                    startCondition.signal();
                    endCondition.awaitUninterruptibly();
                } finally {
                    lock.unlock();
                }
            } finally {
                r.writeEnd();
            }
        }
    }
}
