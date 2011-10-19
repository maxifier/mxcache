package com.maxifier.mxcache.proxy;

import com.maxifier.mxcache.InstanceProvider;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.CacheContextImpl;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.02.11
 * Time: 10:43
 */
@Test
public class ProxyManagerUTest {
    static class ProxyFactoryImpl implements ProxyFactory {
        @Override
        public Object proxy(Class expected, Resolvable value) {
            return "proxy";
        }
    }

    @SuppressWarnings( { "UnusedDeclaration" })
    @UseProxy(ProxyFactoryImpl.class)
    private Object test() {
        return "test";
    }

    @Test
    public void testGetProxyFactory() {
        InstanceProvider instanceProvider = mock(InstanceProvider.class);
        CacheContext context = new CacheContextImpl(instanceProvider);
        when(instanceProvider.forClass(ProxyFactoryImpl.class)).thenReturn(new ProxyFactoryImpl());
        // noinspection unchecked
        ProxyFactory<String> proxy = ProxyManager.getInstance().getProxyFactory(context, getClass(), "test", "()V");
        Assert.assertEquals(proxy.proxy(String.class, new ConstResolvable<String>("test")), "proxy");
    }
}
