package com.maxifier.mxcache.regression;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.transform.CompileHelper;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 13.02.12
 * Time: 14:36
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
