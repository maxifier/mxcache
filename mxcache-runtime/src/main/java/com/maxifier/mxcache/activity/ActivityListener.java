package com.maxifier.mxcache.activity;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.07.2010
 * Time: 12:38:40
 */
public interface ActivityListener {
    void started(ActivityScope scope);

    void finished(ActivityScope scope);
}
