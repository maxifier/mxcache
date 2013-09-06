package com.maxifier.mxcache.provider;

import java.lang.annotation.Annotation;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.04.2010
 * Time: 19:36:29
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
     * Читает значение настройки из аннотации.
     * Вызывается, если у метода есть подходящая не перекрытая аннотация.
     * @param annotation аннотация
     * @return значени свойства из аннотации; null означает, что должно использоваться значени, указанное ранее
     * (в xml конфигурации)
     */
    public abstract T getFromAnnotation(A annotation);

    public Class<A> getAnnotationType() {
        return annotationType;
    }
}
