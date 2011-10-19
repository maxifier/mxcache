package com.maxifier.mxcache.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 08.11.2010
 * Time: 17:24:14
 */
@Test
public class FormatHelperUTest {
    public void testFormat() {
        Assert.assertEquals(FormatHelper.formatSize(123), "123");
        Assert.assertEquals(FormatHelper.formatSize(123 * 1024), "123 k");
        Assert.assertEquals(FormatHelper.formatSize(123 * 1024 + 100), "123.1 k");
        Assert.assertEquals(FormatHelper.formatSize(123 * 1024 + 256), "123.3 k");
        Assert.assertEquals(FormatHelper.formatSize(123 * 1024 * 1024), "123 m");
        Assert.assertEquals(FormatHelper.formatSize(123 * 1024 * 1024 + 100 * 1024), "123.1 m");
    }

    public void testParse() {
        Assert.assertEquals(FormatHelper.parseSize("123 "), 123.0);
        Assert.assertEquals(FormatHelper.parseSize("123.5"), 123.5);
        Assert.assertEquals(FormatHelper.parseSize("123k"), 123.0 * 1024);
        Assert.assertEquals(FormatHelper.parseSize("123.5k"), 123.5 * 1024);
        Assert.assertEquals(FormatHelper.parseSize(" 123.5 k"), 123.5 * 1024);
        Assert.assertEquals(FormatHelper.parseSize(" 123.5 K"), 123.5 * 1024);
        Assert.assertEquals(FormatHelper.parseSize(" 123.5 m"), 123.5 * 1024 * 1024);
    }
}
