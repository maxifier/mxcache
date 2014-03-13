/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.util.CodegenHelper;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import gnu.trove.THashSet;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class MxCacheConfigUTest {

    public static final String CONFIG_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<mxcache>\n" +
            "    <rule name=\"r1\">\n" +
            "        <selector>\n" +
            // wildcard here!
            "            <group>Test*</group>" +
            "            <tag>SomeTag</tag>\n" +
            "            <class>com.maxifier.mxcache.config.TestClass</class>\n" +
            "        </selector>\n" +
            "        <trackDependency>NONE</trackDependency>\n" +
            "    </rule>\n" +

            "    <rule name=\"r2\">\n" +
            "        <selector>\n" +
            "            <annotated>SomeAnnotation1</annotated>\n" +
            "        </selector>\n" +
            // DEFAULT не влияет на наследование 
            "        <trackDependency>DEFAULT</trackDependency>\n" +
            "        <resourceDependency>testResource</resourceDependency>\n" +
            "    </rule>\n" +

            "    <rule name=\"r3\">\n" +
            "        <selector>\n" +
            "            <annotated>SomeAnnotation2</annotated>\n" +
            "        </selector>\n" +
            // определенные позднее значения перекрывают более ранние
            "        <trackDependency>STATIC</trackDependency>\n" +
            "    </rule>\n" +

            "</mxcache>";

    public static final String CONFIG_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<mxcache>\n" +
            "    <rule name=\"r4\">\n" +
            "        <selector>\n" +
            "            <annotated>SomeAnnotation2</annotated>\n" +
            "        </selector>\n" +
            // этот конфиг должен перекрыть предыдущие
            "        <trackDependency>INSTANCE</trackDependency>\n" +
            "    </rule>\n" +

            "    <rule name=\"r5\">\n" +
            "        <selector>\n" +
            // этот конфиг нам не подойдет из-за класса - должно быть полное имя
            "            <class>Test*</class>\n" +
            "        </selector>\n" +
            // этот конфиг должен перекрыть предыдущие
            "        <trackDependency>NONE</trackDependency>\n" +
            "    </rule>\n" +

            "    <rule name=\"r6\">\n" +
            "        <selector>\n" +
            // а этот - подойдет
            "            <class>com.maxifier.mxcache.config.Test*</class>\n" +
            "        </selector>\n" +
            // этот конфиг должен перекрыть предыдущие
            "        <trackDependency>DEFAULT</trackDependency>\n" +
            "    </rule>\n" +

            "    <rule name=\"r7\">\n" +
            "        <selector>\n" +
            // группа другая! был баг, когда этот конфиг проходил
            "            <group>JustAnotherGroup</group>\n" +
            "        </selector>\n" +
            "        <trackDependency>STATIC</trackDependency>\n" +
            "        <resourceDependency>testResource2</resourceDependency>\n" +
            "    </rule>\n" +

            "</mxcache>";

    private static <T> Set<T> set(T... ts) {
        Set<T> set = new THashSet<T>(ts.length);
        for (T t : ts) {
            set.add(t);
        }
        return set;
    }

    public void testLoad() throws Exception {
        JAXBContext context = MxCacheConfigProviderImpl.getJAXBContext();
        MxCacheConfig config = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_1));
        config.setSource("testSource");
        List<RuleWithSelector> rules = config.getRules();
        assert rules.size() == 3;
        RuleWithSelector rule0 = rules.get(0);
        assert rule0.getSource().equals("testSource");
        List<Selector> sel0 = rule0.getSelectors();
        assert sel0.size() == 1;
        Assert.assertEquals(sel0.get(0).toString(), "class:com.maxifier.mxcache.config.TestClass, group:Test*, tag:SomeTag");
    }

    public void testMergeDependency() throws Exception {
        JAXBContext context = MxCacheConfigProviderImpl.getJAXBContext();
        MxCacheConfig config1 = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_1));
        MxCacheConfig config2 = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_2));
        MxCacheConfig config = merge(config1, config2);
        Rule r = new TestMxCacheConfigProvider(config).getRule(TestClass.class, "JustAnotherGroup", new String[] { "@SomeAnnotation1" });
        assert r.getRuleNames().equals(set("r2", "r6", "r7"));
        assert r.getTrackDependency() == DependencyTracking.STATIC;
        assert r.getResourceDependencies().equals(set("testResource", "testResource2"));
    }

    public void testMergeRules() throws Exception {
        JAXBContext context = MxCacheConfigProviderImpl.getJAXBContext();
        MxCacheConfig config = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_1));
        Rule r1 = new TestMxCacheConfigProvider(config).getRule(TestClass.class, "Test1", new String[] { "SomeTag", "@SomeAnnotation1" });
        assert r1.getTrackDependency() == DependencyTracking.NONE;
        assert r1.getRuleNames().equals(set("r1", "r2"));

        Rule r2 = new TestMxCacheConfigProvider(config).getRule(TestClass.class, "Test1", new String[] { "SomeTag", "@SomeAnnotation2" });
        assert r2.getTrackDependency() == DependencyTracking.STATIC;
        assert r2.getRuleNames().equals(set("r1", "r3"));
    }

    public void testMergeConfigs() throws Exception {
        JAXBContext context = MxCacheConfigProviderImpl.getJAXBContext();
        MxCacheConfig config1 = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_1));
        MxCacheConfig config2 = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_2));
        MxCacheConfig config = merge(config1, config2);
        TestMxCacheConfigProvider provider = new TestMxCacheConfigProvider(config);
        Rule r1 = provider.getRule(TestClass.class, "Test1", new String[] { "SomeTag", "@SomeAnnotation1" });
        assert r1.getTrackDependency() == DependencyTracking.NONE;
        assert r1.getRuleNames().equals(set("r1", "r2", "r6"));

        Rule r2 = provider.getRule(TestClass.class, "Test1", new String[] { "SomeTag", "@SomeAnnotation2" });
        assert r2.getTrackDependency() == DependencyTracking.INSTANCE;
        assert r2.getRuleNames().equals(set("r1", "r3", "r4", "r6"));
    }

    public void testSeparateClassLoaderConfig() throws Exception {
        JAXBContext context = MxCacheConfigProviderImpl.getJAXBContext();
        MxCacheConfig config1 = (MxCacheConfig) context.createUnmarshaller().unmarshal(new StringReader(CONFIG_1));
        TestMxCacheConfigProvider provider = new TestMxCacheConfigProvider(config1);

        File test = File.createTempFile("test", ".tmp");

        FileUtils.writeStringToFile(test, CONFIG_2);

        final URL url = test.toURI().toURL();

        ClassLoader myClassLoader = new TestClassLoaderWithResource(url);

        Class<Object> testClass2 = CodegenHelper.loadClass(myClassLoader, CodegenHelper.getByteCode(TestClass.class));

        Rule r1 = provider.getRule(testClass2, "g0", new String[] { "@SomeAnnotation1", "@SomeAnnotation2" });
        assert r1.getTrackDependency() == DependencyTracking.INSTANCE;
        assert r1.getRuleNames().equals(set("r2", "r3", "r4", "r6"));

        Rule r2 = provider.getRule(TestClass.class, "g0", new String[] { "@SomeAnnotation1", "@SomeAnnotation2" });
        assert r2.getTrackDependency() == DependencyTracking.STATIC;
        assert r2.getRuleNames().equals(set("r2", "r3"));
    }

    private static MxCacheConfig merge(MxCacheConfig... configs) {
        MxCacheConfig config = new MxCacheConfig();
        for (MxCacheConfig c : configs) {
            config.merge(c);
        }
        return config;
    }

    private static class TestClassLoaderWithResource extends ClassLoader {
        private final URL url;

        public TestClassLoaderWithResource(URL url) {
            this.url = url;
        }

        @Override
        protected Enumeration<URL> findResources(String name) throws IOException {
            return new Enumeration<URL>() {
                private boolean hasMoreElements = true;

                @Override
                public boolean hasMoreElements() {
                    return hasMoreElements;
                }

                @Override
                public URL nextElement() {
                    hasMoreElements = false;
                    return url;
                }
            };
        }
    }
}
