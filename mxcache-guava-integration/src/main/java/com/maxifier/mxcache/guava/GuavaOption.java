/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guava;

import com.google.common.cache.CacheBuilder;

/**
 * GuavaOption
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-09 09:34)
 */
public enum GuavaOption {
    SOFT_VALUES {
        @Override
        public void set(CacheBuilder<?, ?> builder) {
            builder.softValues();
        }
    }, WEAK_KEYS {
        @Override
        public void set(CacheBuilder<?, ?> builder) {
            builder.weakKeys();
        }
    }, WEAK_VALUES {
        @Override
        public void set(CacheBuilder<?, ?> builder) {
            builder.weakValues();
        }
    }, RECORD_STATS {
        @Override
        public void set(CacheBuilder<?, ?> builder) {
            builder.recordStats();
        }
    };

    public abstract void set(CacheBuilder<?, ?> builder);
}
