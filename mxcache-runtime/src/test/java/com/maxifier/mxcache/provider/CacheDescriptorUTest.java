package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.caches.*;
import com.maxifier.mxcache.config.MxCacheConfigProviderImpl;
import com.maxifier.mxcache.config.RuleUTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 01.05.2010
 * Time: 9:23:55
 */
@Test
public class CacheDescriptorUTest {
    @SuppressWarnings( { "UnusedDeclaration" })
    @Retention (RetentionPolicy.RUNTIME)
    public @interface TestAnnotation {
        String p1();
        int p2();
        String[] p3();
    }

    private static final StrategyProperty<String> P_1 = StrategyProperty.create("p1", String.class, "default", TestAnnotation.class, "p1");

    private static final StrategyProperty<Integer> P_2 = StrategyProperty.create("p2", Integer.class, -1, TestAnnotation.class, "p2");

    private static final StrategyProperty<String[]> P_3 = StrategyProperty.create("p3", String[].class, new String[] { "default" }, TestAnnotation.class, "p3");

    private static final StrategyProperty<Double> P_4 = StrategyProperty.create("p4", Double.class, 2.0);

    private static final StrategyProperty<Boolean> P_5 = StrategyProperty.create("p5", Boolean.class, false);

    private static final StrategyProperty<Class> P_6 = StrategyProperty.create("p6", Class.class, Object.class);

    private static final StrategyProperty<Integer> P_7 = StrategyProperty.create("p7", Integer.class, 3);

    private static final StrategyProperty<RetentionPolicy> P_8 = StrategyProperty.create("p8", RetentionPolicy.class, RetentionPolicy.RUNTIME);

    private static final StrategyProperty<Long> P_9 = StrategyProperty.create("p9", Long.class, -1L);

    private static final StrategyProperty<Float> P_10 = StrategyProperty.create("p10", Float.class, 0.3f);

    private static final String EMPTY_RULE = "<rule></rule>";

    private static final String RULE_IMPORTANT = "<rule important=\"true\">" +
            "   <property name=\"p1\" value=\"v1\" />" +
            "   <property name=\"p3\" value=\"scalar\" />" +
            "   <strategy>com.maxifier.mxcache.config.RuleUTest$ImportantStrategy</strategy>" +
            "</rule>";

    private static final String RULE_1 = "<rule>" +
            "   <property name=\"p1\" value=\"v1\" />" +
            "   <property name=\"p2\" value=\"7\" />" +
            "   <property name=\"p3\" value=\"scalar\" />" +
            "   <property name=\"p5\" value=\"true\" />" +
            "   <property name=\"p6\" value=\"java.lang.String\" />" +
            "   <property name=\"p8\" value=\"CLASS\" />" +
            "   <property name=\"p9\" value=\"4\" />" +
            "   <strategy>com.maxifier.mxcache.config.RuleUTest$SomeStrategy</strategy>" +
            "</rule>";

    private static final String RULE_2 = "<rule>" +
            "   <property name=\"p2\" value=\"8\" />" +
            "   <property name=\"p3\">" +
            "       <value>e1</value>" +
            "       <value>e2</value>" +
            "       <value>e3</value>" +
            "   </property>" +
            "   <property name=\"p4\" value=\"3\" />" +
            "   <property name=\"p6\" value=\"no.such.class\" />" +
            "   <property name=\"p7\" value=\"this is not number\" />" +
            "   <property name=\"p8\">" +
            "       <value>81</value>" +
            "       <value>82</value>" +
            "   </property>" +
            "   <strategy>com.maxifier.mxcache.config.RuleUTest$SomeStrategy</strategy>" +
            "</rule>";

    private static final String RULE_3 = "<rule>" +
            "   <property name=\"p8\" value=\"this is not enum constant\" />" +
            "</rule>";

    public void testCacheInterface() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_3), null);
        assert descriptor.getCacheInterface() == ObjectCache.class;
        assert descriptor.getCalculatableInterface() == ObjectCalculatable.class;

        CacheDescriptor<CacheDescriptorUTest> descriptor2 = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, String.class,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_3), null);
        assert descriptor2.getCacheInterface() == ObjectObjectCache.class;
        assert descriptor2.getCalculatableInterface() == ObjectObjectCalculatable.class;
    }

    public void testProperties() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_1), null);
        assert descriptor.getProperty(P_1).equals("v1");
        assert descriptor.getProperty(P_2).equals(7);
        assert descriptor.getProperty(P_4).equals(2.0);
        assert descriptor.getProperty(P_5);
        assert descriptor.getProperty(P_6) == String.class;
        assert descriptor.getProperty(P_8) == RetentionPolicy.CLASS;
        assert descriptor.getProperty(P_9).equals(4L);
        assert descriptor.getProperty(P_10).equals(0.3f);
        assert Arrays.equals(descriptor.getProperty(P_3), new String[] {"scalar"});
    }

    @Test(expectedExceptions = PropertyConvertationException.class)
    public void testInvalidClassProperty() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_2), null);
        descriptor.getProperty(P_6);
    }

    @Test (expectedExceptions = PropertyConvertationException.class)
    public void testInvalidIntProperty() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_2), null);
        descriptor.getProperty(P_7);
    }

    @Test (expectedExceptions = PropertyConvertationException.class)
    public void testInvalidPropertyVectorForScalar() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_2), null);
        descriptor.getProperty(P_8);
    }

    @Test (expectedExceptions = PropertyConvertationException.class)
    public void testInvalidEnumProperty() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testProperties",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_3), null);
        descriptor.getProperty(P_8);
    }

    public void testOverride() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testOverride",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_1, RULE_2), null);
        assert descriptor.getProperty(P_1).equals("v1");
        assert descriptor.getProperty(P_2).equals(8);
        assert descriptor.getProperty(P_4).equals(3.0);
        assert Arrays.equals(descriptor.getProperty(P_3), new String[] { "e1", "e2", "e3" });
    }

    @TestAnnotation(p1 = "a", p2 = 3, p3 = {"e1", "e5"})
    public void testAnnotation() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testAnnotation",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_1, RULE_2), null);
        assert descriptor.getProperty(P_1).equals("a");
        assert descriptor.getProperty(P_2).equals(3);
        assert Arrays.equals(descriptor.getProperty(P_3), new String[] { "e1", "e5"});
    }

    @TestAnnotation(p1 = "a", p2 = 3, p3 = { "e1", "e5" })
    @Strategy(RuleUTest.SomeStrategy.class)
    public void testImportantAnnotation() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testImportantAnnotation",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(RULE_IMPORTANT, RULE_2), null);
        assert descriptor.getStrategyClass() == RuleUTest.ImportantStrategy.class;
        assert descriptor.getProperty(P_1).equals("v1");
        assert descriptor.getProperty(P_2).equals(3);
        assert Arrays.equals(descriptor.getProperty(P_3), new String[] { "scalar" });
    }

    public void testDefault() throws Exception {
        CacheDescriptor<CacheDescriptorUTest> descriptor = new CacheDescriptor<CacheDescriptorUTest>(
                CacheDescriptorUTest.class, 0, null,
                Void.class, new Object(), "testDefault",
                "()V", null, null, null, MxCacheConfigProviderImpl.loadRule(EMPTY_RULE), null);
        assert descriptor.getProperty(P_1).equals("default");
        assert descriptor.getProperty(P_2).equals(-1);
        assert descriptor.getProperty(P_4).equals(2.0);
        assert !descriptor.getProperty(P_5);
        assert descriptor.getProperty(P_6) == Object.class;
        assert descriptor.getProperty(P_7).equals(3);
        assert descriptor.getProperty(P_8) == RetentionPolicy.RUNTIME;
        assert Arrays.equals(descriptor.getProperty(P_3), new String[] { "default" });
    }
}
