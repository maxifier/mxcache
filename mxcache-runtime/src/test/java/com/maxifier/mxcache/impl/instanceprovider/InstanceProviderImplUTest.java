/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.instanceprovider;

import com.maxifier.mxcache.NoSuchInstanceException;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class InstanceProviderImplUTest {
    static class Y {}

    static class X extends Y {
        private final int x;

        public X(int x) {
            this.x = x;
        }

        public int getX() {
            return x;
        }
    }

    @Test
    public void testRegister() {
        X x = new X(0);
        DefaultInstanceProvider.getInstance().bind(X.class).toInstance(x);
        assertSame(DefaultInstanceProvider.getInstance().forClass(X.class), x);
        DefaultInstanceProvider.getInstance().clearBinding(X.class);
    }

    @Test(expectedExceptions = NoSuchInstanceException.class)
    public void testGetUnregistered() {
        DefaultInstanceProvider.getInstance().forClass(X.class);
    }

    @SuppressWarnings({ "unchecked" })
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidBind() {
        ((Binder) DefaultInstanceProvider.getInstance().bind(X.class)).toInstance("213");
    }

    @Test
    public void testToClass() {
        X x1 = new X(0);
        X x2 = new X(0);
        DefaultInstanceProvider.getInstance().bind(Y.class).toClass(X.class);
        DefaultInstanceProvider.getInstance().bind(X.class).toInstance(x1);
        assertSame(DefaultInstanceProvider.getInstance().forClass(Y.class), x1);
        DefaultInstanceProvider.getInstance().bind(X.class).toInstance(x2);
        assertSame(DefaultInstanceProvider.getInstance().forClass(Y.class), x2);
        DefaultInstanceProvider.getInstance().clearBinding(X.class);
        DefaultInstanceProvider.getInstance().clearBinding(Y.class);
    }

    @SuppressWarnings({ "unchecked" })
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidToClass() {
        DefaultInstanceProvider.getInstance().bind(Y.class).toClass((Class)String.class);
    }

    @SuppressWarnings({ "unchecked" })
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testLoopbackToClass() {
        DefaultInstanceProvider.getInstance().bind(X.class).toClass(X.class);
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testToProvider() {
        X x = new X(0);
        Provider p = mock(Provider.class);
        when(p.get()).thenReturn(x);
        DefaultInstanceProvider.getInstance().bind(X.class).toProvider(p);
        assertSame(DefaultInstanceProvider.getInstance().forClass(X.class), x);
        assertSame(DefaultInstanceProvider.getInstance().forClass(X.class), x);
        DefaultInstanceProvider.getInstance().clearBinding(X.class);
        verify(p, times(2)).get();
        verifyNoMoreInteractions(p);
    }
}
