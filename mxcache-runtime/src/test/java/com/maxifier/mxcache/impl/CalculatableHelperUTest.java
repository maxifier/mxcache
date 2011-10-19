package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.asm.Type;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 25.05.11
 * Time: 9:22
 */
public class CalculatableHelperUTest {
    @Test
    public void testGetCalculatableName() throws Exception {
        Assert.assertEquals(CalculatableHelper.getCalculatableName(Type.getObjectType("java/lang/Long"), "m1", 1), "java/lang/Long$Calculable$m1$1");
    }

    @SuppressWarnings( "DollarSignInName" )
    static class Calculable$m1$1 {
    }

    @SuppressWarnings("DollarSignInName")
    static class Calculable$m2$2 {
    }

    @Test
    public void testGetId() throws Exception {
        Assert.assertEquals(CalculatableHelper.getId(Calculable$m1$1.class), new CacheId(CalculatableHelperUTest.class, 1));
        Assert.assertEquals(CalculatableHelper.getId(Calculable$m2$2.class), new CacheId(CalculatableHelperUTest.class, 2));
    }
}
