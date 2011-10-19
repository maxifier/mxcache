package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.impl.caches.def.ObjectObjectWeakTroveStorage;
import com.maxifier.mxcache.impl.caches.def.ObjectStorageImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 16.11.2010
 * Time: 10:33:05
 */
@Test
public class SignatureUTest {
    @Test
    public void testTroveStorages() throws Exception {
        Signature soo = Signature.of(ObjectObjectWeakTroveStorage.class);
        assertEquals(soo, new Signature(Object.class, Object.class));

        Signature so = Signature.of(ObjectStorageImpl.class);
        assertEquals(so, new Signature(null, Object.class));
    }

    @Test
    public void testIsWider() {
        Signature s1 = new Signature(Object.class, String.class);
        Signature s2 = new Signature(Object.class, String.class);
        Signature s3 = new Signature(Object.class, Object.class);

        Signature s4 = new Signature(int.class, Object.class);
        Signature s5 = new Signature(int.class, String.class);

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
    }
}
