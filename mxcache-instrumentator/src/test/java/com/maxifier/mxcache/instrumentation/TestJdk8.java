package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author Aleksey Tomin (aleksey.tomin@cxense.com) (2016-09-14)
 */
public class TestJdk8 {

    @Cached
    int get() {
        return 1;
    }

    @Test
    public void testStreamWithCached() throws Exception {
        Object[] values = Stream.of(1, 2, 3, 4, 5).sorted(Comparator.naturalOrder()).toArray();
        Assert.assertEquals(values.length, 5);
    }
}
