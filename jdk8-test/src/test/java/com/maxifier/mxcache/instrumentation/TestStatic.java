package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.Cached;

import org.testng.annotations.Test;

import java.util.Comparator;
import java.util.stream.Stream;

/**
 * @author Aleksey Tomin (aleksey.tomin@cxense.com) (2016-09-14)
 */
public class TestStatic {

    @Cached
    int get() {
        return 0;
    }

    @Test
    public void testZZZ() throws Exception {
        System.out.println(Stream.of(1, 2, 3, 4, 5).sorted(Comparator.naturalOrder()).toArray());
    }
}
