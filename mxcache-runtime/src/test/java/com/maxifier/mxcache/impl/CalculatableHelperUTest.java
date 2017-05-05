/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.asm.Type;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
