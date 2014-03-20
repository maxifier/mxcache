/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.context.CacheContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@SuppressWarnings( { "unchecked" })
@Test
public class GenericContextResolverUTest {
    public void testSingleContext() {
        CacheContext c1 = mock(CacheContext.class);
        CacheContext c2 = mock(CacheContext.class);

        when(c1.toString()).thenReturn("c1");
        when(c2.toString()).thenReturn("c2");

        ContextResolver cr1 = mock(ContextResolver.class);
        ContextResolver cr2 = mock(ContextResolver.class);
        when(cr1.getContextOwnerClass()).thenReturn(CharSequence.class);
        when(cr2.getContextOwnerClass()).thenReturn(Runnable.class);

        when(cr1.getContext(any(CharSequence.class))).thenReturn(c1);

        when(cr2.getContext(any(Runnable.class))).thenReturn(c2);
        GenericContextResolver r = new GenericContextResolver(Arrays.asList(cr1, cr2));

        Assert.assertSame(r.getContext(mock(CharSequence.class)), c1);
        Assert.assertSame(r.getContext("123"), c1);
        Assert.assertSame(r.getContext(mock(Runnable.class)), c2);
        Assert.assertSame(r.getContext(mock(Object.class)), CacheFactory.getDefaultContext());
    }
}
