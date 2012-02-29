package com.maxifier.mxcache.ideaplugin.inspections;

import com.maxifier.mxcache.ideaplugin.ResourceNameChecker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 29.02.12
 * Time: 22:47
 */
@Test
public class ResourceNameCheckerUTest {
    public void testCommonPatterns() {
        Assert.assertTrue(ResourceNameChecker.isValidGroupOrTagName("cache.test"));
        Assert.assertTrue(ResourceNameChecker.isValidGroupOrTagName("cache.test.a333"));
        Assert.assertTrue(ResourceNameChecker.isValidGroupOrTagName("#cache.test"));
        Assert.assertTrue(ResourceNameChecker.isValidGroupOrTagName("#Cache_Test"));

        Assert.assertFalse(ResourceNameChecker.isValidGroupOrTagName("##Cache_Test"));
        Assert.assertFalse(ResourceNameChecker.isValidGroupOrTagName("cache.test.333"));
        Assert.assertFalse(ResourceNameChecker.isValidGroupOrTagName("cache-test"));
    }
}
