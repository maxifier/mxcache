/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl;

import com.maxifier.mxcache.*;
import com.maxifier.mxcache.caches.Cache;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.CacheContextImpl;
import com.maxifier.mxcache.impl.instanceprovider.DefaultInstanceProvider;
import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.provider.*;
import com.maxifier.mxcache.storage.ObjectObjectStorage;
import com.maxifier.mxcache.storage.ObjectStorage;
import com.maxifier.mxcache.storage.Storage;
import com.maxifier.mxcache.interfaces.Statistics;

import javax.annotation.Nonnull;

import javax.annotation.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.ref.WeakReference;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.testng.Assert.*;
import static org.testng.Assert.fail;

@Test
public class CacheProviderImplUTest {
    private static final String TEST_LOAD_FINGERPRINT = "thisIsStorage";
    private static final int TEST_SIZE_FINGERPRINT = 71;

    private static final int TEST_CALCULATABLE_FINGERPRINT = 0xBABE;
    private static final int TEST_CACHE_FINGERPRINT = 0xCAFE;

    static final CacheProviderInterceptor NOP_INTERCEPTOR = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return null;
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache) {
            return null;
        }
    };

    static final CacheProviderInterceptor NOP_INTERCEPTOR_2 = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return descriptor;
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache) {
            return cache;
        }
    };

    static final CacheProviderInterceptor THROWING_INTERCEPTOR = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            throw new IllegalStateException("Test exception");
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache) {
            return null;
        }
    };

    static final CacheProviderInterceptor THROWING_INTERCEPTOR_2 = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return null;
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache) {
            throw new IllegalStateException("Test exception");
        }
    };

    static final CacheProviderInterceptor OVERRIDE_CALCULABLE = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return descriptor.overrideCalculable(new IntCalculatable() {
                @Override
                public int calculate(Object owner) {
                    return 999;
                }
            });
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, @Nullable T instance, CacheContext context, Cache cache) {
            return null;
        }
    };

    static final CacheProviderInterceptor OVERRIDE_CACHE = new CacheProviderInterceptor() {
        @Nullable
        @Override
        public <T> CacheDescriptor<T> registerCache(CacheDescriptor<T> descriptor) {
            return null;
        }

        @Nullable
        @Override
        public <T> Cache createCache(RegistryEntry<T> registryEntry, final @Nullable T instance, CacheContext context, Cache cache) {
            return new IntCache() {
                @Override
                public int getOrCreate() {
                    return 987;
                }

                @Override
                public int getSize() {
                    return 0;
                }

                @Override
                public CacheDescriptor getDescriptor() {
                    return null;
                }

                @Override
                public void setDependencyNode(DependencyNode node) {

                }

                @Override
                public DependencyNode getDependencyNode() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void invalidate() {

                }

                @Nullable
                @Override
                public Statistics getStatistics() {
                    return null;
                }
            };
        }
    };

    static class Y implements CachingStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, Class<?> ownerClass, CacheDescriptor<T> descriptor) {
            throw new UnsupportedOperationException();
        }
    }

    static class Z extends X {
        Z() {
            super(67);
        }

        Z(int v) {
            super(v);
        }
    }

    static class X implements CachingStrategy {
        private final int v;

        X() {
            this(132);
        }

        X(int v) {
            this.v = v;
        }


        @Nonnull
        @Override
        public synchronized <T> CacheManager<T> getManager(CacheContext context, Class<?> ownerClass, final CacheDescriptor<T> descriptor) {
            return new XManager<T>(descriptor);
        }

        public class XManager<T> implements CacheManager<T> {
            private final CacheDescriptor<T> descriptor;

            public XManager(CacheDescriptor<T> descriptor) {
                this.descriptor = descriptor;
            }

            @Override
            public CacheDescriptor<T> getDescriptor() {
                return descriptor;
            }

            @Override
            public Cache createCache(final @Nullable T owner) {
                return new IntCache() {
                    @Override
                    public int getOrCreate() {
                        return v;
                    }

                    @Override
                    public void setDependencyNode(DependencyNode node) {
                    }

                    @Override
                    public DependencyNode getDependencyNode() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public void invalidate() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public int getSize() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Statistics getStatistics() {
                        return null;
                    }

                    @Override
                    public CacheDescriptor getDescriptor() {
                        return null;
                    }
                };
            }

            @Override
            public String getImplementationDetails() {
                return IntCache.class.getCanonicalName();
            }

            @Override
            public CacheContext getContext() {
                return null;
            }

            @Override
            public Class<?> getOwnerClass() {
                return null;
            }
        }
    }

    @Cached
    @Strategy(X.class)
    public int x() {
        throw new UnsupportedOperationException();
    }

    @Cached
    public int y() {
        throw new UnsupportedOperationException();
    }

    @Cached
    @UseStorageFactory(TestStorageFactory.class)
    public int z() {
        throw new UnsupportedOperationException();
    }

    @Cached
    @UseStorageFactory(InvalidStorageFactory.class)
    public int w() {
        throw new UnsupportedOperationException();
    }

    @UseStorage(TestStorage1.class)
    public String wStorage(String v) {
        return v;
    }

    @UseStorage(TestStorage3.class)
    public String wStorage3(String v) {
        return v;
    }

    @UseStorage(TestStorage2.class)
    public String wStorage2(String v) {
        return v;
    }

    public static class TestStorage1 implements ObjectObjectStorage {

        @Override
        public Object load(Object key) {
            return TEST_LOAD_FINGERPRINT;
        }

        @Override
        public void save(Object key, Object value) {
        }

        @Override
        public void clear() {
        }

        @Override
        public int size() {
            return TEST_SIZE_FINGERPRINT;
        }
    }

    public static class TestStorage3 implements ObjectObjectStorage {
        public TestStorage3(CacheDescriptor descriptor, CacheContext context) {
        }

        @Override
        public Object load(Object key) {
            return TEST_LOAD_FINGERPRINT;
        }

        @Override
        public void save(Object key, Object value) {
        }

        @Override
        public void clear() {
        }

        @Override
        public int size() {
            return TEST_SIZE_FINGERPRINT;
        }
    }

    public static class TestStorage2 implements ObjectObjectStorage {
        @SuppressWarnings({ "UnusedDeclaration" })
        public TestStorage2(String x) {
            fail("This method should not be called");
        }

        @Override
        public Object load(Object key) {
            fail("This method should not be called");
            return null;
        }

        @Override
        public void save(Object key, Object value) {
            fail("This method should not be called");
        }

        @Override
        public void clear() {
            fail("This method should not be called");
        }

        @Override
        public int size() {
            fail("This method should not be called");
            return 0;
        }
    }

    public static class TestStorageFactory implements StorageFactory {
        @SuppressWarnings({ "UnusedDeclaration" })
        public TestStorageFactory(CacheDescriptor descriptor) {
        }

        @Nonnull
        @Override
        public Storage createStorage(Object owner) {
            return new TestStorage();
        }

        @Override
        public String getImplementationDetails() {
            return null;
        }
    }

    public static class InvalidStorageFactory implements StorageFactory {
        @Nonnull
        @Override
        public Storage createStorage(Object owner) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getImplementationDetails() {
            return null;
        }
    }

    public void testCustomManager() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                throw new UnsupportedOperationException();
            }
        }, "z", "()I", null);
        IntCache c = (IntCache) p.createCache(getClass(), 0, this, CacheFactory.getDefaultContext());
        Assert.assertEquals(c.getOrCreate(), TEST_CACHE_FINGERPRINT);
    }

    public void testInvalidCustomManager() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return TEST_CALCULATABLE_FINGERPRINT;
            }
        }, "w", "()I", null);
        // invalid cache manager doesn't create caches, it throws exceptions
        IntCache c = (IntCache) p.createCache(getClass(), 0, this, CacheFactory.getDefaultContext());
        Assert.assertEquals(c.getOrCreate(), TEST_CALCULATABLE_FINGERPRINT);
    }

    @SuppressWarnings({ "unchecked" })
    public void testCustomStorage() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, String.class, String.class, null, null, new ObjectObjectCalculatable<String, String>() {
            @Override
            public String calculate(Object owner, String s) {
                throw new UnsupportedOperationException();
            }
        }, "wStorage", "(Ljava/lang/String;)Ljava/lang/String;", null);

        ObjectObjectCalculatable<String, String> calculatable = mock(ObjectObjectCalculatable.class);

        ObjectObjectCache<String, String> c = (ObjectObjectCache) p.createCache(getClass(), 0, this, CacheFactory.getDefaultContext());
        assertEquals(c.getSize(), TEST_SIZE_FINGERPRINT);
        assertEquals(c.getOrCreate("test"), TEST_LOAD_FINGERPRINT);
        verifyZeroInteractions(calculatable);
    }

    @SuppressWarnings( { "unchecked" })
    public void testCustomStorageWithCustomConstructor() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, String.class, String.class, null, null, new ObjectObjectCalculatable<String, String>() {
            @Override
            public String calculate(Object owner, String s) {
                throw new UnsupportedOperationException();
            }
        }, "wStorage3", "(Ljava/lang/String;)Ljava/lang/String;", null);

        ObjectObjectCalculatable<String, String> calculatable = mock(ObjectObjectCalculatable.class);

        ObjectObjectCache<String, String> c = (ObjectObjectCache) p.createCache(getClass(), 0, this, CacheFactory.getDefaultContext());
        assertEquals(c.getSize(), TEST_SIZE_FINGERPRINT);
        assertEquals(c.getOrCreate("test"), TEST_LOAD_FINGERPRINT);
        verifyZeroInteractions(calculatable);
    }

    @SuppressWarnings({ "unchecked" })
    public void testInvalidStorage() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, String.class, String.class, null, null, new ObjectObjectCalculatable<String, String>() {
            @Override
            public String calculate(Object owner, String v) {
                if (v.equals("test")) {
                    return "it's ok";
                }
                throw new UnsupportedOperationException();
            }
        }, "wStorage2", "(Ljava/lang/String;)Ljava/lang/String;", null);

        ObjectObjectCache<String, String> c = (ObjectObjectCache) p.createCache(getClass(), 0, this, CacheFactory.getDefaultContext());
        // it will use default cause it cannot create TestStorage2
        assertEquals(c.getSize(), 0);
        assertEquals(c.getOrCreate("test"), "it's ok");
        assertEquals(c.getSize(), 1);
    }

    public void testBind() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                throw new UnsupportedOperationException();
            }
        }, "x", "()I", null);


        DefaultInstanceProvider.getInstance().bind(X.class).toInstance(new X(77));

        // для стратегий действует позднее связывание
        IntCache с = (IntCache) p.createCache(this.getClass(), 0, this, CacheFactory.getDefaultContext());
        assert с.getOrCreate() == 77;

        DefaultInstanceProvider.getInstance().clearBinding(X.class);
    }

    public void testGetCaches() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(this.getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 132;
            }
        }, "y", "()I", null);

        // создаем кэщ, чтобы избежать позднего связывания
        Cache c = p.createCache(this.getClass(), 0, this, CacheFactory.getDefaultContext());

        // we will test cache getCaches()
        List<CacheManager> caches = p.getCaches();
        assert caches.size() == 1;
        CacheDescriptor descriptor = caches.get(0).getDescriptor();
        assert descriptor.getDeclaringClass() == this.getClass();
        assert descriptor.getId() == 0;

        assert c != null;
    }

    public void testDefault() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(this.getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 132;
            }
        }, "y", "()I", null);
        
        IntCache с = (IntCache) p.createCache(this.getClass(), 0, this, CacheFactory.getDefaultContext());
        assert с.getOrCreate() == 132;
        assert с.getOrCreate() == 132;
    }

    public void testInterceptorNoOverride() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        p.intercept(NOP_INTERCEPTOR);

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        assertTrue(p.removeInterceptor(NOP_INTERCEPTOR));
        // try to remove interceptor twice
        assertFalse(p.removeInterceptor(NOP_INTERCEPTOR));

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);
    }

    public void testInterceptorThrowsException() {
        CacheProviderImpl p = new CacheProviderImpl(false);

        p.intercept(THROWING_INTERCEPTOR);

        // exception in interceptor must be ignored
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        assertTrue(p.removeInterceptor(THROWING_INTERCEPTOR));
    }

    public void testInterceptorThrowsException2() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        p.intercept(THROWING_INTERCEPTOR_2);

        // exception in interceptor must be ignored

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        assertTrue(p.removeInterceptor(THROWING_INTERCEPTOR_2));

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);
    }

    public void testInterceptorNoOverride2() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        p.intercept(NOP_INTERCEPTOR_2);

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        assertTrue(p.removeInterceptor(NOP_INTERCEPTOR_2));

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);
    }

    public void testInterceptorOverrideCalculable() {
        CacheProviderImpl p = new CacheProviderImpl(false);

        p.intercept(OVERRIDE_CALCULABLE);

        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 999);

        assertTrue(p.removeInterceptor(OVERRIDE_CALCULABLE));

        // descriptor is already changed, removal of interceptor doesn't affect it
        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 999);
    }

    public void testInterceptorOverrideCache() {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                return 14;
            }
        }, "y", "()I", null);

        IntCache cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);

        p.intercept(OVERRIDE_CACHE);

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 987);

        assertTrue(p.removeInterceptor(OVERRIDE_CACHE));

        cache = (IntCache) p.createCache(getClass(), 0, this, null);
        assertEquals(cache.getOrCreate(), 14);
    }

    private static class TestStorage implements ObjectStorage {
        @Override
        public Object load() {
            return TEST_CACHE_FINGERPRINT;
        }

        @Override
        public void save(Object v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return 0;
        }
    }

    @Test
    public void regressionMxCache32() throws InterruptedException {
        CacheProviderImpl p = new CacheProviderImpl(false);
        p.registerCache(getClass(), 0, null, int.class, null, null, new IntCalculatable() {
            @Override
            public int calculate(Object owner) {
                throw new UnsupportedOperationException();
            }
        }, "x", "()I", null);
        InstanceProvider sp = mock(InstanceProvider.class);
        when(sp.forClass(X.class)).thenReturn(new X() {
            @Nonnull
            @Override
            public synchronized <T> CacheManager<T> getManager(final CacheContext context, Class<?> ownerClass, CacheDescriptor<T> descriptor) {
                return new XManager<T>(descriptor) {
                    @Override
                    public String toString() {
                        return context.toString();
                    }
                };
            }
        });
        CacheContextImpl context = spy(new CacheContextImpl(sp));
        p.createCache(getClass(), 0, this, context);
        p.createCache(getClass(), 0, new CacheProviderImplUTest(), context);
        // вызывается один раз, потому что после этого менеджер сохраняется в related
        verify(sp, times(1)).forClass(X.class);
        //noinspection unchecked
        verify(context, times(1)).setRelated(any(CacheContext.ContextRelatedItem.class), any());
        WeakReference<CacheContext> cr = new WeakReference<CacheContext>(context);
        Assert.assertNotNull(cr.get());
        context = null;
        System.gc();
        System.gc();
        Thread.sleep(100);
        Assert.assertNull(cr.get());
    }
}
