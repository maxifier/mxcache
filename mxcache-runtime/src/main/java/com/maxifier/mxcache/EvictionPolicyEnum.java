package com.maxifier.mxcache;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.03.11
 * Time: 15:39
 * <p>This is commonly used enum for strategies (e.g. ehcache strategy uses it).
 */
public enum EvictionPolicyEnum {
    /** indicates that strategy default policy should be used */
    DEFAULT,
    /** evict least recently used */
    LRU,
    /** evict least frequently used */
    LFU,
    /** first in first out */
    FIFO
}
