package com.maxifier.mxcache.clean;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.NonInstrumentedCacheException;
import com.maxifier.mxcache.asm.Type;
import com.maxifier.mxcache.caches.CleaningNode;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import org.testng.annotations.Test;
import org.testng.Assert;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;
import java.util.Arrays;
import java.util.Map;

import gnu.trove.THashMap;
import com.maxifier.mxcache.caches.Cache;

import static com.maxifier.mxcache.clean.CacheCleanerUTest.Cleared.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.03.2010
 * Time: 15:59:44
 */
@Test
public class CacheCleanerUTest {
    interface X {}

    static class O {}

    static class A extends O implements X {
    }

    static class B extends A {}

    public void testInvalidCleanClass() {
        CleanableRegister register = new CleanableRegister();
        // instance may not yet be loaded, so it's ok to have no caches in interface
        register.clearCacheByClass(Object.class);
        register.clearCacheByClass(Runnable.class);
    }

    @Test (expectedExceptions = IllegalArgumentException.class)
    public void testInvalidCleanInstance() {
        CleanableRegister register = new CleanableRegister();
        register.clearCacheByInstance(new Object());
    }

    static class NonInstrumented {
        @Cached
        public int x(int x) {
            return x;
        }
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testCheckNonInstrumented() {
        CleanableRegister.checkNonInstrumentedCaches(NonInstrumented.class);
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByGroup() {
        CleanableRegister register = new CleanableRegister();
        register.clearInstanceByGroup(new NonInstrumented(), "test");
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByTag() {
        CleanableRegister register = new CleanableRegister();
        register.clearInstanceByTag(new NonInstrumented(), "test");
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByInstance() {
        CleanableRegister register = new CleanableRegister();
        register.clearCacheByInstance(new NonInstrumented());
    }

    @Test(expectedExceptions = NonInstrumentedCacheException.class)
    public void testNonInstrumentedByClass() {
        CleanableRegister register = new CleanableRegister();
        register.clearCacheByClass(NonInstrumented.class);
    }

    public void testClearInstanceByTag() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();

        Map<String, ClassCacheIds> tagMap = new THashMap<String, ClassCacheIds>();
        tagMap.put("test", new ClassCacheIds(new int[] {1}, new int[0]));

        register.registerClass(A.class, ca, null, tagMap);

        tagMap = new THashMap<String, ClassCacheIds>();
        tagMap.put("test", new ClassCacheIds(new int[] {0}, new int[0]));

        register.registerClass(B.class, cb, null, tagMap);
        B b = new B();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.clearInstanceByTag(b, "test");

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(DIRTY, DIRTY);

        ca.checkCleaned(b, DIRTY, CLEAR);
        cb.checkCleaned(b, CLEAR, DIRTY);
    }

    public void testClearInstanceByGroup() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();

        Map<String, ClassCacheIds> groupMap = new THashMap<String, ClassCacheIds>();
        groupMap.put("test", new ClassCacheIds(new int[] { 1 }, new int[0]));

        register.registerClass(A.class, ca, groupMap, null);

        groupMap = new THashMap<String, ClassCacheIds>();
        groupMap.put("test", new ClassCacheIds(new int[] { 0 }, new int[0]));

        register.registerClass(B.class, cb, groupMap, null);
        B b = new B();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.clearInstanceByGroup(b, "test");

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(DIRTY, DIRTY);

        ca.checkCleaned(b, DIRTY, CLEAR);
        cb.checkCleaned(b, CLEAR, DIRTY);
    }

    public void testClearSuperClassWithoutCaches() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);

        B b = new B();
        A a = new A();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.registerInstance(a, A.class);

        register.clearCacheByClass(O.class);

        ca.checkCleanedStatic(CLEAR, CLEAR);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, CLEAR, CLEAR);
        cb.checkCleaned(b, CLEAR, CLEAR);

        ca.checkCleaned(a, CLEAR, CLEAR);
        cb.checkCleaned(a, DIRTY, DIRTY);
    }

    public void testClearClass() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);

        B b = new B();

        register.clearCacheByClass(A.class);

        ca.checkCleanedStatic(CLEAR, CLEAR);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, DIRTY, DIRTY);
        cb.checkCleaned(b, DIRTY, DIRTY);

        ca.reset();
        cb.reset();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.clearCacheByClass(A.class);

        ca.checkCleanedStatic(CLEAR, CLEAR);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, CLEAR, CLEAR);
        cb.checkCleaned(b, CLEAR, CLEAR);
    }

    public void testClearInterface() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);
        B b = new B();

        register.clearCacheByClass(X.class);

        ca.checkCleanedStatic(CLEAR, CLEAR);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, DIRTY, DIRTY);
        cb.checkCleaned(b, DIRTY, DIRTY);

        ca.reset();
        cb.reset();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.clearCacheByClass(A.class);

        ca.checkCleanedStatic(CLEAR, CLEAR);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, CLEAR, CLEAR);
        cb.checkCleaned(b, CLEAR, CLEAR);
    }

    public void testClearMiddleClass() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);
        B b = new B();
        A a = new A();

        register.clearCacheByClass(B.class);

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, DIRTY, DIRTY);
        cb.checkCleaned(b, DIRTY, DIRTY);

        ca.checkCleaned(a, DIRTY, DIRTY);
        cb.checkCleaned(a, DIRTY, DIRTY);

        ca.reset();
        cb.reset();

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        register.registerInstance(a, A.class);

        register.clearCacheByClass(B.class);

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(b, CLEAR, CLEAR);
        cb.checkCleaned(b, CLEAR, CLEAR);

        ca.checkCleaned(a, DIRTY, DIRTY);
        cb.checkCleaned(a, DIRTY, DIRTY);
    }

    public void testClearMiddleClassNotIntersected() {
        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks<A>();
        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks<B>();

        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);
        A a = new A();

        register.clearCacheByClass(B.class);

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        ca.checkCleaned(a, DIRTY, DIRTY);
        cb.checkCleaned(a, DIRTY, DIRTY);

        ca.reset();
        cb.reset();

        register.registerInstance(a, A.class);

        register.clearCacheByClass(B.class);

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(CLEAR, CLEAR);

        // a instance methods will not be cleaned 
        ca.checkCleaned(a, DIRTY, DIRTY);
        cb.checkCleaned(a, DIRTY, DIRTY);
    }

    public void testRegisterClassWhileClear() throws Exception {
        final CleanableRegister register = new CleanableRegister();

        final B b = new B();

        final AtomicInteger n = new AtomicInteger();

        final AtomicReference<Thread> ref = new AtomicReference<Thread>();

        final Cleanable<B> cb = new CleanableWithLocks<B>() {
            @Override
            public void clearInstance(int id) {
                if (id == 0) {
                    if (n.get() != 1) {
                        Assert.fail("Just registered class should not be cleaned");
                    }
                    n.incrementAndGet();
                }
            }
        };

        Cleanable<A> ca = new CleanableWithLocks<A>() {
            @Override
            public void clearInstance(int id) {
                if (id == 0) {
                    Thread t = new Thread() {
                        @Override
                        public void run() {
                            register.registerClass(B.class, cb, null, null);
                        }
                    };
                    ref.set(t);
                    t.start();
                    try {
                        // регистрация классов не мешает чистить кэш
                        t.join();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        register.registerClass(A.class, ca, null, null);

        register.clearCacheByInstance(b);

        ref.get().join();

        n.incrementAndGet();

        register.clearCacheByInstance(b);

        assert n.get() == 2;
    }

    public void testRegisterInstanceWhileClear() throws Exception {
        final CleanableRegister register = new CleanableRegister();

        final B b = new B();

        final AtomicInteger n = new AtomicInteger();

        final AtomicReference<Thread> ref = new AtomicReference<Thread>();

        // noinspection unchecked
        Cleanable<A> ca = new CleanableWithLocks() {
            @Override
            public void clearStatic(int id) {
                if (id == 0) {
                    Thread t = new Thread("Registrator thread") {
                        @Override
                        public void run() {
                            register.registerInstance(b, A.class);
                        }
                    };
                    ref.set(t);
                    t.start();
                    try {
                        t.join(100);
                        // поток, регистрирующий экземпляр должен выжить
                        assert t.isAlive();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void clearInstance(int id) {
                if (id == 0) {
                    if (n.get() != 1) {
                        Assert.fail("Just registered object should not be cleaned");
                    }
                    n.incrementAndGet();
                }
            }
        };

        register.registerClass(A.class, ca, null, null);

        register.clearCacheByClass(A.class);

        n.incrementAndGet();

        ref.get().join();

        register.clearCacheByClass(A.class);

        assert n.get() == 2;
    }

    enum Cleared {
        CLEAR, DIRTY
    }

    /**
     * Проверяет, чтобы методы очистки вызывались со всеми необходимыми блокировками
     * @param <T>
     */
    static class CleanableWithLocks<T> implements Cleanable<T> {
        private final Class clazz;

        private final Map<T, InstanceCacheWithLock[]> instanceCaches = new THashMap<T, InstanceCacheWithLock[]>();

        private final StaticCacheWithLock[] staticCaches;

        private final Map<T, Cleared[]> cleaned = new THashMap<T, Cleared[]>();

        private Cleared[] cleanedStatic;

        public CleanableWithLocks() {
            this(null);
        }

        public CleanableWithLocks(Class clazz) {
            this.clazz = clazz;
            staticCaches = new StaticCacheWithLock[2];
            for (int i = 0; i < 2; i++) {
                staticCaches[i] = new StaticCacheWithLock(this, i, clazz);
            }
        }

        @Override
        public void appendInstanceCachesTo(List<CleaningNode> list, T o) {
            list.add(getInstanceCache(o, 0));
            list.add(getInstanceCache(o, 1));
        }

        @Override
        public void appendStaticCachesTo(List<CleaningNode> list) {
            for (Cache cache : staticCaches) {
                list.add(cache);
            }
        }

        public void clearInstance(int i) {

        }

        public void clearStatic(int i) {

        }

        public Cleared[] getCleanedStatic() {
            if (cleanedStatic == null) {
                cleanedStatic = new Cleared[] { DIRTY, DIRTY};
            }
            return cleanedStatic;
        }

        public Cleared[] getCleaned(T instance) {
            Cleared[] row = cleaned.get(instance);
            if (row == null) {
                row = new Cleared[] { DIRTY, DIRTY };
                cleaned.put(instance, row);
            }
            return row;
        }

        @Override
        public Cache getStaticCache(int id) {
            return staticCaches[id];
        }

        @Override
        public Cache getInstanceCache(T o, int id) {
            InstanceCacheWithLock[] row = instanceCaches.get(o);
            if (row == null) {
                row = new InstanceCacheWithLock[2];
                for (int i = 0; i < 2; i++) {
                    row[i] = new InstanceCacheWithLock(this, i, clazz, o);
                }
                instanceCaches.put(o, row);
            }
            return row[id];
        }

        @SuppressWarnings({ "unchecked" })
        public void checkCleaned(O instance, Cleared... booleans) {
            Assert.assertEquals(getCleaned((T)instance), booleans);
        }

        public void checkCleanedStatic(Cleared... booleans) {
            Assert.assertEquals(getCleanedStatic(), booleans);
        }

        public void reset() {
            cleaned.clear();
            cleanedStatic = null;
        }
    }

    /**
     * Проверяет, чтобы клинер действительно получал все локи
     */
    public void testFullLock() {
        B b = new B();

        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks(A.class);

        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks(B.class);
        
        CleanableRegister register = new CleanableRegister();
        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        Lock lock = new ReentrantLock();
        Condition c = lock.newCondition();
        AtomicBoolean bool = new AtomicBoolean();

        startEvilThread(b, ca, cb, lock, c, bool);

        register.clearCacheByClass(B.class);

        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleanedStatic(CLEAR, CLEAR);
        ca.checkCleaned(b, CLEAR, CLEAR);
        cb.checkCleaned(b, CLEAR, CLEAR);
    }

    /** Проверяет, чтобы клинер действительно получал нужные локи при очистке по тегу */
    public void testTagClear() {
        B b = new B();

        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks();

        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks();

        CleanableRegister register = new CleanableRegister();

        Map<String, ClassCacheIds> m = new THashMap<String, ClassCacheIds>();
        m.put("someTag", new ClassCacheIds(new int[]{1}, new int[]{0}));

        register.registerClass(A.class, ca, null, m);
        register.registerClass(B.class, cb, null, null);

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        Lock lock = new ReentrantLock();
        Condition c = lock.newCondition();
        AtomicBoolean bool = new AtomicBoolean();

        startEvilThread(b, ca, cb, lock, c, bool);

        register.clearCacheByTag("someTag");

        cb.checkCleaned(b, DIRTY, DIRTY);
        cb.checkCleanedStatic(DIRTY, DIRTY);
        ca.checkCleaned(b, DIRTY, CLEAR);
        ca.checkCleanedStatic(CLEAR, DIRTY);
    }

    private @interface MyAnnotation {
        // важно, что класс именно внутренний - тут можно протестирова, чтобы тег брался именно по internal name,
        // а не canonical name.
    }

    public void testAnnotationClear() {
        B b = new B();

        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks();

        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks();

        CleanableRegister register = new CleanableRegister();

        Map<String, ClassCacheIds> m = new THashMap<String, ClassCacheIds>();
        m.put("@" + Type.getType(MyAnnotation.class).getInternalName().replace('/', '.'), new ClassCacheIds(new int[] { 1 }, new int[] { 0 }));

        register.registerClass(A.class, ca, null, m);
        register.registerClass(B.class, cb, null, null);

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        Lock lock = new ReentrantLock();
        Condition c = lock.newCondition();
        AtomicBoolean bool = new AtomicBoolean();

        startEvilThread(b, ca, cb, lock, c, bool);

        register.clearCacheByAnnotation(MyAnnotation.class);

        cb.checkCleaned(b, DIRTY, DIRTY);
        cb.checkCleanedStatic(DIRTY, DIRTY);
        ca.checkCleaned(b, DIRTY, CLEAR);
        ca.checkCleanedStatic(CLEAR, DIRTY);
    }

    /** Проверяет, чтобы клинер действительно получал нужные локи при очистке по группе */
    public void testGroupClear() {
        B b = new B();

        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks();

        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks();

        CleanableRegister register = new CleanableRegister();

        register.registerClass(A.class, ca, null, null);

        Map<String, ClassCacheIds> m = new THashMap<String, ClassCacheIds>();
        m.put("someGroup", new ClassCacheIds(new int[] { 1 }, new int[] { 0 }));
        register.registerClass(B.class, cb, m, null);

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        Lock lock = new ReentrantLock();
        Condition c = lock.newCondition();
        AtomicBoolean bool = new AtomicBoolean();

        startEvilThread(b, ca, cb, lock, c, bool);

        register.clearCacheByGroup("someGroup");

        ca.checkCleaned(b, DIRTY, DIRTY);
        ca.checkCleanedStatic(DIRTY, DIRTY);
        cb.checkCleaned(b, DIRTY, CLEAR);
        cb.checkCleanedStatic(CLEAR, DIRTY);
    }

    /** Проверяет, чтобы клинер действительно получал нужные локи при очистке по группе */
    public void testInstanceClear() {
        B b = new B();

        // noinspection unchecked
        CleanableWithLocks<A> ca = new CleanableWithLocks();

        // noinspection unchecked
        CleanableWithLocks<B> cb = new CleanableWithLocks();

        CleanableRegister register = new CleanableRegister();

        register.registerClass(A.class, ca, null, null);
        register.registerClass(B.class, cb, null, null);

        register.registerInstance(b, A.class);
        register.registerInstance(b, B.class);

        Lock lock = new ReentrantLock();
        Condition c = lock.newCondition();
        AtomicBoolean bool = new AtomicBoolean();

        startEvilThread(b, ca, cb, lock, c, bool);

        register.clearCacheByInstance(b);

        cb.checkCleaned(b, CLEAR, CLEAR);
        ca.checkCleaned(b, CLEAR, CLEAR);
        
        cb.checkCleanedStatic(DIRTY, DIRTY);
        ca.checkCleanedStatic(DIRTY, DIRTY);
    }

    private void checkEvilThreadStarted(Lock lock, Condition c, AtomicBoolean bool) {
        lock.lock();
        try {
            while (!bool.get()) {
                c.awaitUninterruptibly();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Запускает злобный поток, который в течении некоторого времени (30 мс) блокирует некоторые "кэши", не давая
     * клинеру получить к ним доступ
     */
    @SuppressWarnings ({ "JavaDoc" })
    private void startEvilThread(final B b, final Cleanable<A> ca, final Cleanable<B> cb, final Lock lock, final Condition c, final AtomicBoolean bool) {
        Thread t = new Thread("My evil thread") {
            @Override
            public void run() {
                // выполним несколько блокировок-разблокировок. Клинер нас должен покорно ждать.
                try {
                    lock.lock();
                    try {
                        ca.getInstanceCache(b, 0).getLock().lock();
                        bool.set(true);
                        c.signal();
                    } finally {
                        lock.unlock();
                    }
                    Thread.sleep(10);
                    cb.getInstanceCache(b, 1).getLock().lock();
                    ca.getInstanceCache(b, 0).getLock().unlock();

                    Thread.sleep(10);

                    ca.getStaticCache(1).getLock().lock();

                    Thread.sleep(10);

                    cb.getInstanceCache(b, 1).getLock().unlock();
                    ca.getStaticCache(1).getLock().unlock();
                } catch (Throwable e) {
                    Assert.fail();
                }
            }
        };
        t.start();
        checkEvilThreadStarted(lock, c, bool);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidRegisterCache() {
        CleanableRegister register = new CleanableRegister();
        register.registerInstance("123", String.class);
    }

    public void testBatchLock() throws InterruptedException {
        final List<Lock> locks = Arrays.<Lock>asList(new ReentrantLock(), new ReentrantLock(), new ReentrantLock());

        class WaiterThread extends Thread {
            boolean locked;

            @Override
            public void run() {
                CleaningHelper.lock(locks);
                locked = true;
            }
        }

        locks.get(0).lock(); // locked: 0

        WaiterThread t = new WaiterThread();
        t.setDaemon(true);
        t.start();

        locks.get(1).lock(); // locked: 0, 1

        t.join(10);

        locks.get(2).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(0).unlock(); // locked: 1, 2

        t.join(10);

        locks.get(0).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(1).unlock(); // locked: 0, 2

        t.join(10);

        locks.get(0).unlock(); // locked: 2

        t.join(10);

        locks.get(0).lock(); // locked: 0, 2

        t.join(10);

        locks.get(1).lock(); // locked: 0, 1, 2

        t.join(10);

        locks.get(2).unlock(); // locked: 0, 1

        t.join(10);

        locks.get(1).unlock(); // locked: 0

        t.join(10);
        assert !t.locked;

        locks.get(0).unlock(); // nothing locked

        t.join();
        assert t.locked;
    }

    private static class InstanceCacheWithLock extends CacheWithLock {
        private final Class clazz;
        private final int id;
        private final CleanableWithLocks cleanableWithLocks;
        private final Object instance;

        public InstanceCacheWithLock(CleanableWithLocks cleanableWithLocks, int id, Class clazz, Object instance) {
            super(new ReentrantLock());
            this.cleanableWithLocks = cleanableWithLocks;
            this.id = id;
            this.clazz = clazz;
            this.instance = instance;
        }

        @Override
        public void setDependencyNode(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings({ "unchecked" })
        @Override
        public void clear() {
            super.clear();
            cleanableWithLocks.getCleaned(instance)[id] = Cleared.CLEAR;
            cleanableWithLocks.clearInstance(id);
        }

        @Override
        public String toString() {
            return (clazz == null ? "?" : clazz.getSimpleName()) + "@instance[" + id + "]";
        }
    }

    private static class StaticCacheWithLock extends CacheWithLock {
        private final Class clazz;
        private final int id;
        private final CleanableWithLocks cleanableWithLocks;

        public StaticCacheWithLock(CleanableWithLocks cleanableWithLocks, int id, Class clazz) {
            super(new ReentrantLock());
            this.cleanableWithLocks = cleanableWithLocks;
            this.id = id;
            this.clazz = clazz;
        }

        @Override
        public void setDependencyNode(DependencyNode node) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            super.clear();
            cleanableWithLocks.getCleanedStatic()[id] = Cleared.CLEAR;
            cleanableWithLocks.clearStatic(id);
        }

        @Override
        public String toString() {
            return (clazz == null ? "?" : clazz.getSimpleName()) + "@static[" + id + "]";
        }
    }
}
