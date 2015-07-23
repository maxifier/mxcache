/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.caches.IntCache;
import com.maxifier.mxcache.caches.IntObjectCache;
import com.maxifier.mxcache.caches.ObjectCache;
import com.maxifier.mxcache.caches.ObjectObjectCache;
import com.maxifier.mxcache.impl.caches.def.ObjectObjectWeakTroveStorage;
import com.maxifier.mxcache.impl.caches.def.ObjectStorageImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class SignatureUTest {
    @Test
    public void testTroveStorages() throws Exception {
        Signature soo = Signature.of(ObjectObjectWeakTroveStorage.class);
        assertSame(soo, Signature.of(Object.class, Object.class));

        Signature so = Signature.of(ObjectStorageImpl.class);
        assertSame(so, Signature.of(null, Object.class));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNull() {
        new Signature(null, null);
    }

    @Test
    public void testErased() {
        Signature s = Signature.of(String.class, String.class);
        assertSame(s.getValue(), String.class);
        assertSame(s.getContainer(), String.class);
        assertSame(s.erased(), Signature.of(Object.class, Object.class));
    }

    @Test
    public void testGetCacheInterface() {
        assertSame(Signature.of(String.class, String.class).getCacheInterface(), ObjectObjectCache.class);
        assertSame(Signature.of(int.class, String.class).getCacheInterface(), IntObjectCache.class);
        assertSame(Signature.of(null, String.class).getCacheInterface(), ObjectCache.class);
        assertSame(Signature.of(null, int.class).getCacheInterface(), IntCache.class);
    }

    @Test
    public void testIsWider() {
        Signature s1 = Signature.of(Object.class, String.class);
        Signature s2 = Signature.of(Object.class, String.class);
        Signature s3 = Signature.of(Object.class, Object.class);

        Signature s4 = Signature.of(int.class, Object.class);
        Signature s5 = Signature.of(int.class, String.class);
        Signature s6 = Signature.of(null, String.class);
        Signature s7 = Signature.of(null, Object.class);

        assertTrue(s1.isWider(s2));
        assertTrue(s2.isWider(s1));

        assertTrue(s3.isWider(s1));
        assertTrue(s3.isWider(s2));

        assertFalse(s1.isWider(s3));
        assertFalse(s2.isWider(s3));

        assertFalse(s1.isWider(s4));
        assertFalse(s4.isWider(s1));

        assertFalse(s5.isWider(s4));
        assertTrue(s4.isWider(s5));

        assertFalse(s5.isWider(s6));
        assertFalse(s6.isWider(s5));

        assertTrue(s7.isWider(s6));
        assertFalse(s6.isWider(s7));
    }
}
