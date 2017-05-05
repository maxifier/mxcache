/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;

/**
 * <p>AnnotationProperty are like StrategyProperty but they allow to use custom annotation to override the value of
 * property.</p>
 * <p>In most cases you should not extend this class directly, use
 * {@link com.maxifier.mxcache.provider.ReflectiveAnnotationProperty} instead.</p>
 *
 * @see ReflectiveAnnotationProperty
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class AnnotationProperty<A extends Annotation, T> extends StrategyProperty<T> {
    private final Class<A> annotationType;

    protected AnnotationProperty(String name, Class<T> type, Class<A> annotationType, T defaultValue) {
        super(name, type, defaultValue);
        if (!Annotation.class.isAssignableFrom(annotationType)) {
            throw new IllegalArgumentException(annotationType + " is not annotation type");
        }
        this.annotationType = annotationType;
    }

    protected AnnotationProperty(String name, Class<T> type, Class<A> annotationType) {
        this(name, type, annotationType, null);
    }

    /**
     * Gets the value from annotation. Implement this method for your custom annotations.
     * @param annotation your custom annotation.
     * @return the value of property; null means that MxCache should lookup the value in xml configuration
     */
    public abstract T getFromAnnotation(@Nonnull A annotation);

    public Class<A> getAnnotationType() {
        return annotationType;
    }
}
