package com.maxifier.mxcache;

import com.maxifier.mxcache.MxCache;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 25.10.2010
 * Time: 10:56:11
 */
@Test
public class MxCacheVersionUTest {
    public void test() {
        Assert.assertTrue(MxCache.getVersion().matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }
}
