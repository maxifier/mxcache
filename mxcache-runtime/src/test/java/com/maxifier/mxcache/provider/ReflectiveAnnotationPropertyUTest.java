/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
@ReflectiveAnnotationPropertyUTest.TestAnnotation(value = 3, str = "test")
public class ReflectiveAnnotationPropertyUTest {
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testNonAnnotation() {
        //noinspection unchecked
        StrategyProperty.create("length", int.class, (Class)String.class, "length");
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
        int value();

        String str();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvalidType() {
        StrategyProperty.create("value", int.class, TestAnnotation.class, "str");
    }

    public void testPrimitives1() {
        AnnotationProperty<TestAnnotation, Integer> property = StrategyProperty.create("value", int.class, TestAnnotation.class, "value");
        Assert.assertEquals(property.getAnnotationType(), TestAnnotation.class);
        TestAnnotation a = ReflectiveAnnotationPropertyUTest.class.getAnnotation(TestAnnotation.class);
        Assert.assertEquals(property.getFromAnnotation(a), Integer.valueOf(3));
    }

    public void testPrimitives2() {
        AnnotationProperty<TestAnnotation, Integer> property = StrategyProperty.create("value", Integer.class, TestAnnotation.class, "value");
        Assert.assertEquals(property.getAnnotationType(), TestAnnotation.class);
        TestAnnotation a = ReflectiveAnnotationPropertyUTest.class.getAnnotation(TestAnnotation.class);
        Assert.assertEquals(property.getFromAnnotation(a), Integer.valueOf(3));
    }

    public void testObject() {
        AnnotationProperty<TestAnnotation, String> property = StrategyProperty.create("str", String.class, TestAnnotation.class, "str");
        Assert.assertEquals(property.getAnnotationType(), TestAnnotation.class);
        TestAnnotation a = ReflectiveAnnotationPropertyUTest.class.getAnnotation(TestAnnotation.class);
        Assert.assertEquals(property.getFromAnnotation(a), "test");
    }
}
