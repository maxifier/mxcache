/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 * @author Aleksey Tomin (aleksey.tomin@cxense.com)
 */
public final class SmartReferenceManager {
    private static volatile ReferenceQueue QUEUE;

    private SmartReferenceManager() {}

    /**
     * Reference queue for correct remove objects from ObjectObjectTroveStorage.
     * Result is null before the first call {@link #switchOn()}
     */
    @Nullable
    public static <T> ReferenceQueue<T> getReferenceQueue() {
        return QUEUE;
    }

    /**
     * Create queue and run thread
     */
    public static void switchOn() {
        if (QUEUE == null) {
            synchronized (SmartReferenceManager.class) {
                if (QUEUE == null) {
                    QUEUE =  new ReferenceQueue();
                    Thread t = new ReferenceThread();
                    t.setDaemon(true);
                    t.start();
                }
            }
        }
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
                    SmartReference r = (SmartReference) SmartReferenceManager.getReferenceQueue().remove();
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
