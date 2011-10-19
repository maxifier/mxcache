package com.maxifier.mxcache.transform;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 15:12:58
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
