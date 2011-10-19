package com.maxifier.mxcache.test;

import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;
import com.maxifier.mxcache.CacheFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 13:01:48
 */
@Test
public class MixedCacheKeyFTest {
    private String prefix;

    @Cached
    private String get(int v, String suffix) {
        return prefix + v + suffix;
    }

    public void test() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        prefix = "p";
        assert get(3, "s").equals("p3s");
        assert get(4, "s").equals("p4s");

        prefix = "g";

        assert get(3, "s").equals("p3s");
        assert get(4, "s").equals("p4s");

        CacheFactory.getCleaner().clearCacheByClass(MixedCacheKeyFTest.class);

        assert get(3, "s").equals("g3s");
        assert get(4, "s").equals("g4s");
    }
}
