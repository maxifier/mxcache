package com.maxifier.mxcache;

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
    public void testFormat() {
        Assert.assertTrue(MxCache.getVersion().matches("\\d+\\.\\d+\\.\\d+(-SNAPSHOT)?"));
    }

    public void testCompareVersion() {
        eq("2.2.2", "2.2.2");
        less("2.2.2", "2.2.3");
        less("2.2.2-SNAPSHOT", "2.2.3-SNAPSHOT");
        less("2.2.2-SNAPSHOT", "2.2.23-SNAPSHOT");
        less("2.2.2-SNAPSHOT", "2.2.13-SNAPSHOT");
        eq("2.2.2-SNAPSHOT", "2.2.2-SNAPSHOT");
        eq("2.2.2-b4", "2.2.2-b4");
        less("2.2.2-b4", "2.2.2");
        less("2.2.2-b4", "2.2.2-b5");
        less("2.2.2-b4", "2.2.2-b13");
        less("2.2.2-b4x", "2.2.2-b13x");
        less("2.2.2-a", "2.2.2-b");
    }
    
    private static void less(String s1, String s2) {
        Version v1 = v(s1);
        Version v2 = v(s2);
        Assert.assertEquals(v1.compareTo(v2), -1);
        Assert.assertEquals(v2.compareTo(v1),  1);
    }

    private static void eq(String s1, String s2) {
        Version v1 = v(s1);
        Version v2 = v(s2);
        Assert.assertEquals(v1, v2);
        Assert.assertEquals(v2, v1);
        Assert.assertEquals(v1.compareTo(v2),  0);
        Assert.assertEquals(v2.compareTo(v1),  0);
    }

    private static Version v(String s) {
        return new Version(s);
    }
}
