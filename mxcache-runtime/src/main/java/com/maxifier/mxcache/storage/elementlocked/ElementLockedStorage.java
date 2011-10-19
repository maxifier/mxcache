package com.maxifier.mxcache.storage.elementlocked;

import com.maxifier.mxcache.storage.Storage;

import java.util.concurrent.locks.Lock;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.11.2010
 * Time: 9:32:08
 */
public interface ElementLockedStorage extends Storage {
    Lock getLock();
}
