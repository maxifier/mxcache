package com.maxifier.mxcache.scheduler;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 10.11.11
 * Time: 13:18
 * To change this template use File | Settings | File Templates.
 */
public interface MxScheduler {
    void schedule(Runnable runnable);
}
