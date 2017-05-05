/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import javax.annotation.Nonnull;

import java.lang.annotation.Annotation;

/**
 * A generic user caching strategy property.
 *
 * @see CacheDescriptor#getProperty(StrategyProperty)
 * @see CacheDescriptor#getProperty(AnnotationProperty)
 * @see AnnotationProperty
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StrategyProperty<T> {
    /**
     * Property name to be used in mxcache.xml descriptor.
     */
    private final String name;

    /**
     * Property value type
     */
    private final Class<T> type;

    /**
     * Default value of the property.
     * Will be used if no definition is found in either corresponding annotation if any or mxcache.xml.
     * It's up to strategy to allow user to leave some properties initialized to default.
     */
    private final T defaultValue;

    /**
     * @param name property name to be used in mxcache.xml descriptor
     * @param type property value type
     * @param defaultValue default value of the property
     */
    public StrategyProperty(@Nonnull String name, @Nonnull Class<T> type, T defaultValue) {
        if (defaultValue != null && !type.isInstance(defaultValue)) {
            throw new IllegalArgumentException("Invalid defaultValue = " + defaultValue + " for property " + name + ": " + type);
        }
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a property with null-default value.
     *
     * @param name property name to be used in mxcache.xml descriptor.
     * @param type property value type
     */
    public StrategyProperty(String name, Class<T> type) {
        this(name, type, null);
    }

    public static <T> StrategyProperty<T> create(@Nonnull String name, @Nonnull Class<T> type, T defaultValue) {
        return new StrategyProperty<T>(name, type, defaultValue);
    }

    public static <T> StrategyProperty<T> create(@Nonnull String name, @Nonnull Class<T> type) {
        return new StrategyProperty<T>(name, type);
    }

    public static <A extends Annotation, T> AnnotationProperty<A, T> create(@Nonnull String name, @Nonnull Class<T> type, Class<A> annotationClass, String annotationPropertyName) {
        return new ReflectiveAnnotationProperty<A, T>(name, type, null, annotationClass, annotationPropertyName);
    }

    public static <A extends Annotation, T> AnnotationProperty<A, T> create(@Nonnull String name, @Nonnull Class<T> type, T defaultValue, Class<A> annotationClass, String annotationPropertyName) {
        return new ReflectiveAnnotationProperty<A, T>(name, type, defaultValue, annotationClass, annotationPropertyName);
    }

    /**
     * @return property name to be used in mxcache.xml descriptor.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Property value type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * @return default value of the property.
     *          Will be used if no definition is found in either corresponding annotation if any or mxcache.xml.
     *          It's up to strategy to allow user to leave some properties initialized to default.
     */
    public T getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StrategyProperty that = (StrategyProperty) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name + ": " + type;
    }
}