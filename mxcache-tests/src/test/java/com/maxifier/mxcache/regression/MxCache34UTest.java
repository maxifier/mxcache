package com.maxifier.mxcache.regression;

import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.transform.CompileHelper;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 13.02.12
 * Time: 14:36
 */
@Test
public class MxCache34UTest {
    @Cached 
    public static int x() {
        return 321;
    }
    
    public void testRegressionMxCache34() throws Exception {
        String pkg = MxCache34UTest.class.getPackage().getName();
        CompileHelper.compile(pkg + ".Test34",
                        "package " + pkg + ";\n" +
                        "public class Test34 {\n" +
                        "   public static int t() {\n" +
                        "       return " + MxCache34UTest.class.getName() + ".x();\n" +
                        "   }\n" +
                        "}");
    }
}
