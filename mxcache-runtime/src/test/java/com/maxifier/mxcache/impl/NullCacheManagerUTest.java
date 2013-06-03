package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.ObjectIntCache;
import com.maxifier.mxcache.caches.ObjectIntCalculatable;
import com.maxifier.mxcache.provider.Signature;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.mockito.Mockito.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 03.03.11
 * Time: 18:08
 */
@Test
public class NullCacheManagerUTest {
    @SuppressWarnings( { "unchecked" })
    @Test
    public void testGetImplementation() throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Constructor<? extends Cache> ctor = NullCacheManager.getImplementation(new Signature(String.class, int.class));
        ObjectIntCalculatable calculable = mock(ObjectIntCalculatable.class);


        ObjectIntCache cache1 = (ObjectIntCache) ctor.newInstance("owner1", calculable);
        ObjectIntCache cache2 = (ObjectIntCache) ctor.newInstance("owner2", calculable);

        when(calculable.calculate("owner1", "123")).thenReturn(3, 4);
        when(calculable.calculate("owner2", "123")).thenReturn(5, 6);

        Assert.assertEquals(cache1.getOrCreate("123"), 3);
        Assert.assertEquals(cache1.getOrCreate("123"), 4);
        Assert.assertEquals(cache2.getOrCreate("123"), 5);
        Assert.assertEquals(cache2.getOrCreate("123"), 6);
        Assert.assertEquals(cache1.getCacheOwner(), "owner1");
        Assert.assertEquals(cache2.getCacheOwner(), "owner2");
    }
}
