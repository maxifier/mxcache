package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.PublicAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 9:39:24
*/
public final class BasicTransforms {
    private BasicTransforms(){}

    private static final ReferenceQueue QUEUE = new ReferenceQueue();

    static {
        Thread t = new ReferenceThread();
        t.setDaemon(true);
        t.start();
    }

    @SuppressWarnings({"UnusedParameters"})
    @PublicAPI
    // used in Ignore
    public static Object ignore(Object o) {
        return null;
    }

    @PublicAPI
    // used in SoftKey
    public static <T> SoftReference<T> createSoftReference(T t) {
        return new SmartSoftReference<T>(t, QUEUE);
    }

    @PublicAPI
    // used in WeakKey
    public static <T> WeakReference<T> createWeakReference(T t) {
        return new SmartWeakReference<T>(t, QUEUE);
    }

    private static class ReferenceThread extends Thread {
        private static final Logger logger = LoggerFactory.getLogger(ReferenceThread.class);

        public ReferenceThread() {
            super("MxCache smart reference watcher");
        }

        @SuppressWarnings({ "InfiniteLoopStatement" })
        @Override
        public void run() {
            try {
                while (true) {
                    SmartReference r = (SmartReference) QUEUE.remove();
                    try {
                        Runnable c = r.getCallback();
                        if (c != null) {
                            c.run();
                        }
                    } catch (Throwable t) {
                        logger.error("Error in reference thread", t);
                    }
                }
            } catch (InterruptedException e) {
                // it's ok, we will exit
            }
        }
    }
}
