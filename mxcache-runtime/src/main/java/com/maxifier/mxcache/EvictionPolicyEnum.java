/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
