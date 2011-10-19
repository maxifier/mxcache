package com.maxifier.mxcache.size;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.08.2010
 * Time: 13:20:55
 */
@Test
public class ObjectSizeHelperUTest {
    @Sizable
    public static class TestClass {
        int x;

        boolean a;
        
        boolean b;

        byte c;

        long d;
    }

    public void testString() {
        // array "123" is padded to 8 bytes + 12 header -> 20
        // String has int fields offset, count and hash, char[] value -> 16 + 8 header
        // total = 20 + 16 + 8 = 44
        Assert.assertEquals(ObjectSizeHelper.getApproximateSize("123").getSize(), 44);
    }

    public void testClassWithPrimitives() {
        // 8 for header
        // 4 for x
        // 4 for a and b
        // 8 for d
        Assert.assertEquals(ObjectSizeHelper.getApproximateSize(new TestClass()).getSize(), 24);
    }
}
