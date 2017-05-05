/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class MxSchedulerImpl implements MxScheduler {
    private static final Logger logger = LoggerFactory.getLogger(MxSchedulerImpl.class);

    private final BlockingQueue<Runnable> tasks = new LinkedBlockingDeque<Runnable>();
    private Thread schedulerThread;

    @Override
    public synchronized void schedule(Runnable runnable) {
        tasks.offer(runnable);
        if (schedulerThread == null || !schedulerThread.isAlive()) {
            startThread();
        }
    }

    private void startThread() {
        schedulerThread = new Thread(new SchedulerRunner(), "MxCache IO Scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
    }

    private class SchedulerRunner implements Runnable {
        @SuppressWarnings({"InfiniteLoopStatement"})
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable r = tasks.take();
                    try {
                        r.run();
                    } catch (Throwable e) {
                        logger.error("Cannot execute scheduled task " + r, e);
                    }
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }
}
