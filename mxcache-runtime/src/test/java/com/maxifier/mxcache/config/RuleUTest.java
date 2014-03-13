/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.context.CacheContext;
import com.maxifier.mxcache.resource.TrackDependency;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.provider.CacheDescriptor;
import com.maxifier.mxcache.provider.CacheManager;
import com.maxifier.mxcache.provider.CachingStrategy;

import javax.annotation.Nonnull;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class RuleUTest {
    public static class SomeStrategy implements CachingStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            throw new UnsupportedOperationException();
        }
    }

    public static class OtherStrategy implements CachingStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            throw new UnsupportedOperationException();
        }
    }

    public static class ImportantStrategy implements CachingStrategy {
        @Nonnull
        @Override
        public <T> CacheManager<T> getManager(CacheContext context, CacheDescriptor<T> descriptor) {
            throw new UnsupportedOperationException();
        }
    }

    private static final String RULE_1 = "<rule>" +
            "   <trackDependency>STATIC</trackDependency>" +
            "   <property name=\"p0\" value=\"v0\" />" +
            "   <property name=\"p1\" value=\"v1\" />" +
            "   <property name=\"array\">" +
            "       <value>e1</value>" +
            "       <value>e2</value>" +
            "       <value>e3</value>" +
            "   </property>" +
            "   <disableCache>true</disableCache>" +
            "   <strategy>com.maxifier.mxcache.config.RuleUTest$SomeStrategy</strategy>" +
            "   <cacheName>my_name</cacheName>" +
            "</rule>";

    private static final String RULE_IMPORTANT = "<rule important=\"true\">" +
            "   <trackDependency>INSTANCE</trackDependency>" +
            "   <property name=\"p2\" value=\"important value\" />" +
            "   <strategy>com.maxifier.mxcache.config.RuleUTest$ImportantStrategy</strategy>" +
            "   <disableCache>true</disableCache>" +
            "   <cacheName>name_from_rule</cacheName>" +
            "</rule>";

    private static final String RULE_2 = "<rule>" +
            "   <trackDependency>NONE</trackDependency>" +
            "   <property name=\"p1\" value=\"v2\" />" +
            "   <property name=\"array\" value=\"not_array\" />" +
            "   <property name=\"p2\" value=\"v3\" />" +
            "   <disableCache>false</disableCache>" +
            "</rule>";

    public void testProperty() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_1);

        assert r.getProperty("p1").equals("v1");
        assert r.getProperty("array").equals(Arrays.asList("e1", "e2", "e3"));
        
        assert r.getStrategy() == SomeStrategy.class;
        assert r.getDisableCache();
    }

    @Strategy(OtherStrategy.class)
    @TrackDependency(DependencyTracking.NONE)
    public void testOverrideStrategyWithMethod() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_1);

        r.override(RuleUTest.class.getMethod("testOverrideStrategyWithMethod"), "test");

        assert r.getStrategy() == OtherStrategy.class;
        assert r.getTrackDependency() == DependencyTracking.NONE;
        assert r.getDisableCache();
        Assert.assertEquals(r.getCacheName(), "test");
    }

    public void testOverrideProperty() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_1, RULE_2);
        assert r.getTrackDependency() == DependencyTracking.NONE;
        assert r.getProperty("p0").equals("v0");
        assert r.getProperty("p1").equals("v2");
        assert r.getProperty("array").equals("not_array");
        assert r.getProperty("p2").equals("v3");

        assert r.getStrategy() == SomeStrategy.class;
        assert !r.getDisableCache();
    }

    public void testLoad() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_1);
        assert r.getTrackDependency() == DependencyTracking.STATIC;
        assert r.getProperty("p0").equals("v0");
        assert r.getProperty("p1").equals("v1");
        assert r.getProperty("array").equals(Arrays.asList("e1", "e2", "e3"));
        assert r.getStrategy() == SomeStrategy.class;
        assert r.getDisableCache();
    }

    public void testOverrideImportantProperty() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_1, RULE_IMPORTANT, RULE_2);
        assert r.getTrackDependency() == DependencyTracking.INSTANCE;
        assert r.getProperty("p0").equals("v0");
        assert r.getProperty("p1").equals("v2");
        assert r.getProperty("array").equals("not_array");
        assert r.getProperty("p2").equals("important value");

        assert r.getStrategy() == ImportantStrategy.class;
        assert r.getDisableCache();
    }

    @Strategy(OtherStrategy.class)
    @TrackDependency(DependencyTracking.NONE)
    public void testOverrideImportantStrategyWithMethod() throws Exception {
        Rule r = MxCacheConfigProviderImpl.loadRule(RULE_IMPORTANT);

        r.override(RuleUTest.class.getMethod("testOverrideImportantStrategyWithMethod"), "testRule");

        assert r.getStrategy() == ImportantStrategy.class;
        assert r.getTrackDependency() == DependencyTracking.INSTANCE;
        Assert.assertEquals(r.getCacheName(), "name_from_rule");
    }
}
