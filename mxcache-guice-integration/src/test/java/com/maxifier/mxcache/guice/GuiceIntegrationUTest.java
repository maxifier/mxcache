/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.guice;

import com.google.inject.*;
import com.maxifier.mxcache.Cached;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.context.UseCacheContext;
import com.maxifier.mxcache.impl.DefaultStrategy;
import com.maxifier.mxcache.impl.NullCacheManager;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * GuiceIntegrationUTest
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class GuiceIntegrationUTest {
    public static class TestStrategy implements CachingStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            return new NullCacheManager<T>(descriptor);
        }
    }

    public static class StrategyOverride extends TestStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            return DefaultStrategy.getInstance().getManager(context, descriptor);
        }
    }

    public static class X {
        @Inject
        public X(@UseCacheContext CacheContext c) {

        }

        public X() {
        }

        private int x;

        @Cached
        @Strategy(TestStrategy.class)
        public int get() {
            return x++;
        }
    }

    public static class Y extends X {
        @Inject
        public Y(ChildCacheContext c) {
            super(c);
        }
    }

    @Test
    public void testSimpleGuiceIntegration() {
        Injector injector = Guice.createInjector(new MxCacheGuiceModule(), new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestStrategy.class).to(StrategyOverride.class);
            }
        });

        X nonGuice = new X();

        Assert.assertEquals(nonGuice.get(), 0);
        Assert.assertEquals(nonGuice.get(), 1);
        Assert.assertEquals(nonGuice.get(), 2);
        Assert.assertEquals(nonGuice.get(), 3);

        X instance = injector.getInstance(X.class);
        Assert.assertEquals(instance.get(), 0);
        Assert.assertEquals(instance.get(), 0);
        Assert.assertEquals(instance.get(), 0);
        Assert.assertEquals(instance.get(), 0);
    }

    @Test
    public void testSimpleGuiceIntegrationChildInjector() {
        Injector injector = Guice.createInjector(new MxCacheGuiceModule());

        X instance = injector.getInstance(X.class);
        Assert.assertEquals(instance.get(), 0);
        Assert.assertEquals(instance.get(), 1);
        Assert.assertEquals(instance.get(), 2);
        Assert.assertEquals(instance.get(), 3);

        Injector child = injector.createChildInjector(new MxCacheGuiceChildModule(ChildCacheContext.class), new AbstractModule() {
            @Override
            protected void configure() {
                bind(TestStrategy.class).to(StrategyOverride.class);
                bind(Y.class);
            }
        });

        X childInstance = child.getInstance(Y.class);
        Assert.assertEquals(childInstance.get(), 0);
        Assert.assertEquals(childInstance.get(), 0);
        Assert.assertEquals(childInstance.get(), 0);
        Assert.assertEquals(childInstance.get(), 0);
    }

    @Test
    public void testSimpleGuiceContextNames() {
        Injector injector = Guice.createInjector(new MxCacheGuiceModule("Parent"));
        Assert.assertEquals(injector.getInstance(CacheContext.class).toString(), "Parent");
        Injector child = injector.createChildInjector(new MxCacheGuiceChildModule(ChildCacheContext.class, "Child"));
        Assert.assertEquals(child.getInstance(ChildCacheContext.class).toString(), "Child");
    }

    interface ChildCacheContext extends CacheContext {}
}
