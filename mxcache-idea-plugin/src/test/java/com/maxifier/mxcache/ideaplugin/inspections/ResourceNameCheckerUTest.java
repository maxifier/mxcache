/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.ideaplugin.inspections;

import com.maxifier.mxcache.ideaplugin.ResourceNameChecker;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
