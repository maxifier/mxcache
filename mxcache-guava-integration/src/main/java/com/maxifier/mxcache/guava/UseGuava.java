/*
 * Copyright (c) 2008-2013 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guava;

import com.google.common.cache.Weigher;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * UseGuava - add this annotation to your method among with @Cached to make your method return values to be cached.
 * <p>
 * All methods in this class directly correspond to methods in {@link com.google.common.cache.CacheBuilder}, see
 * this class for details. Javadocs there are copied from this class.
 * </p>
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2012-10-08 18:14)
 * @see com.google.common.cache.CacheBuilder
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseGuava {
    /**
     * Specifies the maximum number of entries the cache may contain. Note that the cache <b>may evict
     * an entry before this limit is exceeded</b>. As the cache size grows close to the maximum, the
     * cache evicts entries that are less likely to be used again. For example, the cache may evict an
     * entry because it hasn't been used recently or very often.
     *
     * <p>When {@code size} is zero, elements will be evicted immediately after being loaded into the
     * cache. This can be useful in testing, or to disable caching temporarily without a code change.
     *
     * <p>This feature cannot be used in conjunction with {@link #maxWeight}.
     */
    long maxSize() default -1;

    /**
     * Specifies the maximum weight of entries the cache may contain. Weight is determined using the
     * {@link Weigher} specified with {@link #weigher}, and use of this method requires a
     * corresponding call to {@link #weigher}.
     * <p/>
     * <p>Note that the cache <b>may evict an entry before this limit is exceeded</b>. As the cache
     * size grows close to the maximum, the cache evicts entries that are less likely to be used
     * again. For example, the cache may evict an entry because it hasn't been used recently or very
     * often.
     * <p/>
     * <p>When {@code weight} is zero, elements will be evicted immediately after being loaded into
     * cache. This can be useful in testing, or to disable caching temporarily without a code
     * change.
     * <p/>
     * <p>Note that weight is only used to determine whether the cache is over capacity; it has no
     * effect on selecting which entry should be evicted next.
     * <p/>
     * <p>This feature cannot be used in conjunction with {@link #maxSize}.
     */
    long maxWeight() default -1;

    /**
     * Guides the allowed concurrency among update operations. Used as a hint for internal sizing. The
     * table is internally partitioned to try to permit the indicated number of concurrent updates
     * without contention. Because assignment of entries to these partitions is not necessarily
     * uniform, the actual concurrency observed may vary. Ideally, you should choose a value to
     * accommodate as many threads as will ever concurrently modify the table. Using a significantly
     * higher value than you need can waste space and time, and a significantly lower value can lead
     * to thread contention. But overestimates and underestimates within an order of magnitude do not
     * usually have much noticeable impact. A value of one permits only one thread to modify the cache
     * at a time, but since read operations and cache loading computations can proceed concurrently,
     * this still yields higher concurrency than full synchronization.
     * <p/>
     * <p> Defaults to 4. <b>Note:</b>The default may change in the future. If you care about this
     * value, you should always choose it explicitly.
     * <p/>
     * <p>The current implementation uses the concurrency level to create a fixed number of hashtable
     * segments, each governed by its own write lock. The segment lock is taken once for each explicit
     * write, and twice for each cache loading computation (once prior to loading the new value,
     * and once after loading completes). Much internal cache management is performed at the segment
     * granularity. For example, access queues and write queues are kept per segment when they are
     * required by the selected eviction algorithm. As such, when writing unit tests it is not
     * uncommon to specify {@code concurrencyLevel(1)} in order to achieve more deterministic eviction
     * behavior.
     * <p/>
     * <p>Note that future implementations may abandon segment locking in favor of more advanced
     * concurrency controls.
     */
    int concurrencyLevel() default -1;

    /**
     * Sets the minimum total size for the internal hash tables. For example, if the initial capacity
     * is {@code 60}, and the concurrency level is {@code 8}, then eight segments are created, each
     * having a hash table of size eight. Providing a large enough estimate at construction time
     * avoids the need for expensive resizing operations later, but setting this value unnecessarily
     * high wastes memory.
     */
    int initialCapacity() default -1;

    /**
     * Specifies that each entry should be automatically removed from the cache once a fixed duration
     * has elapsed after the entry's creation, the most recent replacement of its value, or its last
     * access. Access time is reset by all cache read and write operations (including
     * {@code Cache.asMap().get(Object)} and {@code Cache.asMap().put(K, V)}), but not by operations
     * on the collection-views of {@link com.google.common.cache.Cache#asMap}.
     * <p/>
     * <p>When {@code expireAfterAccess} is zero, this method hands off to
     * {@link #maxSize()}{@code (0)}, ignoring any otherwise-specificed maximum
     * size or weight. This can be useful in testing, or to disable caching temporarily without a code
     * change.
     * <p/>
     * <p>Expired entries may be counted in {@link com.google.common.cache.Cache#size}, but will never be visible to read or
     * write operations. Expired entries are cleaned up as part of the routine maintenance described
     * in the class javadoc.
     * <p/>
     * <b>Time unit for this property is milliseconds</b>.
     */
    long expireAfterAccess() default -1;

    /**
     * Specifies that each entry should be automatically removed from the cache once a fixed duration
     * has elapsed after the entry's creation, or the most recent replacement of its value.
     * <p/>
     * <p>When {@code expireAfterWrite} is zero, this method hands off to
     * {@link #maxSize()}{@code (0)}, ignoring any otherwise-specificed maximum
     * size or weight. This can be useful in testing, or to disable caching temporarily without a code
     * change.
     * <p/>
     * <p>Expired entries may be counted in {@link com.google.common.cache.Cache#size}, but will never be visible to read or
     * write operations. Expired entries are cleaned up as part of the routine maintenance described
     * in the class javadoc.
     * <p/>
     * <b>Time unit for this property is milliseconds</b>.
     */
    long expireAfterWrite() default -1;

    /**
     * Specifies that active entries are eligible for automatic refresh once a fixed duration has
     * elapsed after the entry's creation, or the most recent replacement of its value. The semantics
     * of refreshes are specified in {@link com.google.common.cache.LoadingCache#refresh}, and are performed by calling
     * {@link com.google.common.cache.CacheLoader#reload}.
     * <p/>
     * <p>As the default implementation of {@link com.google.common.cache.CacheLoader#reload} is synchronous, it is
     * recommended that users of this method override {@link com.google.common.cache.CacheLoader#reload} with an asynchronous
     * implementation; otherwise refreshes will be performed during unrelated cache read and write
     * operations.
     * <p/>
     * <p>Currently automatic refreshes are performed when the first stale request for an entry
     * occurs. The request triggering refresh will make a blocking call to {@link com.google.common.cache.CacheLoader#reload}
     * and immediately return the new value if the returned future is complete, and the old value
     * otherwise.
     * <p/>
     * <p><b>Note:</b> <i>all exceptions thrown during refresh will be logged and then swallowed</i>.
     * <b>Time unit for this property is milliseconds</b>.
     */
    long refreshAfterWrite() default -1;

    /**
     * Specifies the weigher to use in determining the weight of entries. Entry weight is taken
     * into consideration by {@link #maxWeight()} when determining which entries to evict, and
     * use of this method requires a corresponding call to {@link #maxWeight()}. Weights are measured
     * and recorded when entries are inserted into the
     * cache, and are thus effectively static during the lifetime of a cache entry.
     * <p/>
     * <p>When the weight of an entry is zero it will not be considered for size-based eviction
     * (though it still may be evicted by other means).
     * <p/>
     * <p><b>Important note:</b> Instead of returning <em>this</em> as a {@code CacheBuilder}
     * instance, this method returns {@code CacheBuilder<K1, V1>}. From this point on, either the
     * original reference or the returned reference may be used to complete configuration and build
     * the cache, but only the "generic" one is type-safe. That is, it will properly prevent you from
     * building caches whose key or value types are incompatible with the types accepted by the
     * weigher already provided; the {@code CacheBuilder} type cannot do this. For best results,
     * simply use the standard method-chaining idiom, as illustrated in the documentation at top,
     * configuring a {@code CacheBuilder} and building your {@link com.google.common.cache.Cache} all in a single statement.
     * <p/>
     * <p><b>Warning:</b> if you ignore the above advice, and use this {@code CacheBuilder} to build
     * a cache whose key or value type is incompatible with the weigher, you will likely experience
     * a {@link ClassCastException} at some <i>undefined</i> point in the future.
     */
    Class<? extends Weigher> weigher() default NoWeigher.class;

    GuavaOption[] options() default {};

    interface NoWeigher extends Weigher {
    }
}
