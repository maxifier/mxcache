/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.util.CodegenHelper;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ReflectiveAnnotationProperty<A extends Annotation, T> extends AnnotationProperty<A, T> {
    private final Method method;

    public ReflectiveAnnotationProperty(String name, Class<T> type, T defaultValue, Class<A> annotationType, String annotationPropertyName) {
        this(name, type, defaultValue, annotationType, getMethod(annotationType, annotationPropertyName));
    }

    public ReflectiveAnnotationProperty(String name, Class<T> type, T defaultValue, Class<A> annotationType, Method method) {
        super(name, type, annotationType, defaultValue);
        if (!canBeCasted(type, method)) {
            throw new IllegalArgumentException("Method " + method + " cannot be casted to " + type);
        }
        this.method = method;
    }

    private boolean canBeCasted(Class<T> type, Method method) {
        return box(type).isAssignableFrom(box(method.getReturnType()));
    }

    private static Class box(Class type) {
        Class boxed = CodegenHelper.getBoxedType(type);
        return boxed == null ? type : boxed;
    }

    private static <A extends Annotation> Method getMethod(Class<A> annotationClass, String annotationPropertyName) {
        try {
            return annotationClass.getMethod(annotationPropertyName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("No such property: " + annotationClass + "." + annotationPropertyName, e);
        }
    }

    @Override
    public T getFromAnnotation(@Nonnull A annotation) {
        try {
            //noinspection unchecked
            return (T) method.invoke(annotation);
        } catch (IllegalAccessException e) {
            throw new PropertyConvertationException(e);
        } catch (InvocationTargetException e) {
            throw new PropertyConvertationException(e);
        }
    }
}
