/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.activity;

import javax.annotation.Nonnull;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public interface Activity extends Serializable {
    /**
     * Activity name is immutable.
     *
     * @return activity name
     */
    @Nonnull
    String getName();

    /**
     * Marks this activity as active in given scope.
     *
     * If activity was already run in the same or wider scope, no changes made otherwise listeners are notified.
     *
     * @param scope activity scope
     */
    void start(@Nonnull ActivityScope scope);

    /**
     * Marks this activity as finished in a given scope.
     *
     * Activities started in thread-local scope should be finished in the same thread.
     *
     * For each start() invocation there must be corresponding finish() invocation in the same scope.
     *
     * @param scope activity scope
     * @throws IllegalStateException if activity is not running in given scope
     */
    void finish(@Nonnull ActivityScope scope);

    /**
     * @return true, if activity is running
     */
    boolean isRunning();

    /**
     * <p>
     * Adds a listener. Listener is notified every time the activity is started or finished.</p>
     * <p>
     * If activity was started/finished in thread-local scope the listener will be notified in the same thread.
     * This is not true for global scope - in this case thread different from the one called "start" or "finish" may
     * be used to notify listener.</p>
     * <p>
     * Listeners are notified BEFORE activity is actually started or finished. I.e. isRunning will return false in
     * listener's "started" and true in listener's "finished" method.</p>
     * <p>
     * All exceptions (but not errors!) thrown by listeners are ignored.</p>
     */
    void addListener(ActivityListener listener);

    /**
     * Removes listener.
     */
    void removeListener(ActivityListener listener);
}
