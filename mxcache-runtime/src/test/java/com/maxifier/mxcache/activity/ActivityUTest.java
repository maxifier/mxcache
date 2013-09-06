package com.maxifier.mxcache.activity;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 11:24:42
 */
@Test
public class ActivityUTest {
    @Test(expectedExceptions = IllegalStateException.class)
    public void testFinishWithoutStart() {
        Activity a = ActivityTracker.getActivity("test");
        a.finish(ActivityScope.GLOBAL);
    }

    public void testLocalStart() {
        Activity a = ActivityTracker.getActivity("test");
        assert !a.isRunning();
        a.start(ActivityScope.THREAD_LOCAL);
        try {
            assert a.isRunning();
        } finally {
            a.finish(ActivityScope.THREAD_LOCAL);
        }
        assert !a.isRunning();
    }

    public void testLocalStartMultiThread() throws InterruptedException {
        final Activity a = ActivityTracker.getActivity("test");
        final ReentrantLock lock = new ReentrantLock();
        final Condition condition = lock.newCondition();
        final AtomicInteger b = new AtomicInteger();
        Thread t = new Thread() {
            @Override
            public void run() {
                assert !a.isRunning();
                waitCondition(lock, condition, b, 1);
                assert !a.isRunning();
                a.start(ActivityScope.THREAD_LOCAL);
                try {
                    assert a.isRunning();
                    signalCondition(lock, condition, b, 2);
                } finally {
                    a.finish(ActivityScope.THREAD_LOCAL);
                }
                assert !a.isRunning();
            }
        };
        t.start();

        assert !a.isRunning();
        a.start(ActivityScope.THREAD_LOCAL);
        try {
            assert a.isRunning();
            signalCondition(lock, condition, b, 1);
            waitCondition(lock, condition, b, 2);
            assert a.isRunning();
        } finally {
            a.finish(ActivityScope.THREAD_LOCAL);
        }
        assert !a.isRunning();

        t.join();
    }

    private static void waitCondition(ReentrantLock lock, Condition condition, AtomicInteger c, int v) {
        lock.lock();
        try {
            while (c.get() != v) {
                condition.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    private static void signalCondition(ReentrantLock lock, Condition condition, AtomicInteger c, int v) {
        lock.lock();
        try {
            c.set(v);
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void testGlobalStart() {
        Activity a = ActivityTracker.getActivity("test");
        a.start(ActivityScope.GLOBAL);
        try {
            assert a.isRunning();
        } finally {
            a.finish(ActivityScope.GLOBAL);
        }
    }

    private static Event event(boolean start, ActivityScope scope) {
        return new Event(start, scope);
    }

    static class Event {
        final boolean start;

        final ActivityScope scope;

        Event(boolean start, ActivityScope scope) {
            this.start = start;
            this.scope = scope;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Event event = (Event) o;
            return start == event.start && scope == event.scope;
        }

        @Override
        public int hashCode() {
            return 31 * (start ? 1 : 0) + scope.hashCode();
        }
    }

    public void testListener() {
        Activity a = ActivityTracker.getActivity("test");

        final List<Event> events = new ArrayList<Event>();
        ActivityListener listener = new ActivityListener() {
            @Override
            public void started(ActivityScope scope) {
                events.add(event(true, scope));
            }

            @Override
            public void finished(ActivityScope scope) {
                events.add(event(false, scope));
            }
        };
        a.addListener(listener);
        try {
            a.start(ActivityScope.GLOBAL);
            assert events.size() == 1;
            assert events.get(0).equals(event(true, ActivityScope.GLOBAL));

            a.start(ActivityScope.THREAD_LOCAL);
            assert events.size() == 2;
            assert events.get(1).equals(event(true, ActivityScope.THREAD_LOCAL));

            a.finish(ActivityScope.GLOBAL);
            assert events.size() == 3;
            assert events.get(2).equals(event(false, ActivityScope.GLOBAL));

            a.finish(ActivityScope.THREAD_LOCAL);
            assert events.size() == 4;
            assert events.get(3).equals(event(false, ActivityScope.THREAD_LOCAL));
        } finally {
            a.removeListener(listener);
        }

        a.start(ActivityScope.GLOBAL);
        a.finish(ActivityScope.GLOBAL);
        // удалили слушателя - размер не меняется
        assert events.size() == 4;
    }

    public void testThreadLocalListener() {
        final Thread t = Thread.currentThread();
        Activity a = ActivityTracker.getActivity("test");
        ActivityListener listener = Mockito.spy(new TestActivityListener(t));
        a.addListener(listener);
        try {
            a.start(ActivityScope.THREAD_LOCAL);
            try {
                Mockito.verify(listener).started(ActivityScope.THREAD_LOCAL);
            } finally {
                a.finish(ActivityScope.THREAD_LOCAL);
            }
            Mockito.verify(listener).finished(ActivityScope.THREAD_LOCAL);
            Mockito.verifyNoMoreInteractions(listener);
        } finally {
            a.removeListener(listener);
        }
    }

    public void testEquality() {
        Activity r1 = ActivityTracker.getActivity("test");
        Activity r2 = ActivityTracker.getActivity("test");
        // конечно лучше не сравнивать так, однако раз ActivityTracker гарантирует это,
        // то надо убедиться
        assert r1 == r2;

        // авось
        assert r1.equals(r2);

        // на всякий случай
        assert r1.getName().equals("test");

        // эти строки имеют одинаковый хэш, но это не важно
        Activity r3 = ActivityTracker.getActivity("0-42L");
        Activity r4 = ActivityTracker.getActivity("0-43-");
        assert !r3.equals(r4);
        assert !r4.equals(r3);
    }

    public void testSerialization() throws Exception {
        Activity r1 = ActivityTracker.getActivity("test");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        try {
            oos.writeObject(r1);
        } finally {
            oos.close();
        }
        Activity r2;
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        try {
            r2 = (Activity) ois.readObject();
        } finally {
            ois.close();
        }
        // сериализация не должна приводить к ложным дубликатам ресурсов
        assert r1 == r2;
    }

    private static class TestActivityListener implements ActivityListener {
        private final Thread t;

        public TestActivityListener(Thread t) {
            this.t = t;
        }

        @Override
        public void started(ActivityScope scope) {
            assert Thread.currentThread() == t;
        }

        @Override
        public void finished(ActivityScope scope) {
            assert Thread.currentThread() == t;
        }
    }
}