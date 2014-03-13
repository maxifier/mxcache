/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.transform;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class CompileHelperUTest {
    public interface Interface {
        public String x();
    }

    public void testCompilation() throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        Class c = CompileHelper.compile("Test", "public class Test implements com.maxifier.mxcache.transform.CompileHelperUTest.Interface {public String x() {return \"test\"; }}");
        Interface v = (Interface) c.newInstance();
        Assert.assertEquals(v.x(), "test");
    }
}
