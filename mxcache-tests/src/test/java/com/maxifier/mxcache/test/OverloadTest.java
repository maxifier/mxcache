package com.maxifier.mxcache.test;

import com.maxifier.mxcache.CacheFactory;
import com.maxifier.mxcache.Cached;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 23.08.2010
 * Time: 17:24:01
 */
@Test
public class OverloadTest {
    @Cached
    private String x(String x) {
        return x;
    }

    @Cached
    private String x(Integer x) {
        return x.toString();
    }

    public void test() {
        CacheFactory.getCleaner().clearCacheByInstance(this);

        assert x(3).equals("3");
        assert x("3").equals("3");
    }
}
