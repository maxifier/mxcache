/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.benchmark;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.MxCache;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-27 16:51)
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@State(Scope.Benchmark)
@Fork(0)
public class MxCacheBenchmark {
    private static BigInteger fact(int value) {
        BigInteger r = BigInteger.ONE;
        for (int i = 1; i < value; i++) {
            r = r.multiply(BigInteger.valueOf(i));
        }
        return r;
    }

    private BigInteger[] cache = new BigInteger[1100];
    private final static BigInteger[] STATIC_CACHE = new BigInteger[1100];

    private BigInteger factManualCache(int value) {
        BigInteger v = cache[value];
        if (v == null) {
            v = fact(value);
            cache[value] = v;
        }
        return v;
    }

    private static BigInteger factStaticManualCache(int value) {
        BigInteger v = STATIC_CACHE[value];
        if (v == null) {
            v = fact(value);
            STATIC_CACHE[value] = v;
        }
        return v;
    }

    @Cached
    private BigInteger factMxCache(int value) {
        return fact(value);
    }

    @Cached
    private static BigInteger factStaticMxCache(int value) {
        return fact(value);
    }

    @GenerateMicroBenchmark
    public int manualCache() {
        int val = 0;
        for (int i = 0; i<1000; i++) {
            val ^= factManualCache(i).hashCode();
        }
        return val;
    }

    @GenerateMicroBenchmark
    public int staticManualCache() {
        int val = 0;
        for (int i = 0; i<1000; i++) {
            val ^= factStaticManualCache(i).hashCode();
        }
        return val;
    }

    @GenerateMicroBenchmark
    public int mxCache() {
        int val = 0;
        for (int i = 0; i<1000; i++) {
            val ^= factMxCache(i).hashCode();
        }
        return val;
    }

    @GenerateMicroBenchmark
    public int staticMxCache() {
        int val = 0;
        for (int i = 0; i<1000; i++) {
            val ^= factStaticMxCache(i).hashCode();
        }
        return val;
    }

    @GenerateMicroBenchmark
    public void cleanManualCache() {
        for (int i = 0; i<100; i++) {
            Arrays.fill(cache, null);
        }
    }

    @GenerateMicroBenchmark
    public void cleanMxCache() {
        for (int i = 0; i<100; i++) {
            MxCache.getCleaner().clearCacheByInstance(this);
        }
    }

    public static void main(String[] args) throws IOException, RunnerException {
        org.openjdk.jmh.Main.main(args);
    }
}
