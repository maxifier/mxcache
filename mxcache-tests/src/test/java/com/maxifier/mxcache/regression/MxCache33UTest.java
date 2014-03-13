/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.regression;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.transform.CompileHelper;
import org.testng.annotations.Test;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class MxCache33UTest {
    @Cached 
    public static int x() {
        return 321;
    }
    
    public void testRegressionMxCache33() throws Exception {
        CompileHelper.compile("myTest.Test33",
                        "package myTest;\n" +
                        "import " + MxCache33UTest.class.getName() + ".*; \n" +
                        "public class Test33 {}");
    }
}
