package com.maxifier.mxcache.transform;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 19.10.2010
 * Time: 10:33:35
 */
public interface SmartReference {
    Runnable getCallback();

    void setCallback(Runnable callback);
}
