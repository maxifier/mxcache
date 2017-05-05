/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.ReferenceQueue;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class SmartReferenceManager {
    private static final ReferenceQueue QUEUE = new ReferenceQueue();

    static {
        Thread t = new ReferenceThread();
        t.setDaemon(true);
        t.start();
    }

    private SmartReferenceManager() {}

    public static <T> ReferenceQueue<T> getReferenceQueue() {
        return QUEUE;
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
