package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.03.11
 * Time: 19:41
 */
public class CacheFactoryUTest {
    @Test
    public void testSetDefaultContext() throws Exception {
        CacheContext defaultContext = CacheFactory.getDefaultContext();
        Assert.assertNotNull(defaultContext);
        CacheContext mock = Mockito.mock(CacheContext.class);
        CacheFactory.setDefaultContext(mock);
        Assert.assertSame(CacheFactory.getDefaultContext(), mock);
        CacheFactory.setDefaultContext(null);
        Assert.assertSame(CacheFactory.getDefaultContext(), defaultContext);
    }
}
