package com.maxifier.mxcache.size;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.02.2009
 * Time: 15:35:29
 */
public interface SizeIterator {
    <T> void pass(Object key, T sizable);
}
