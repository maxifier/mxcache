package com.maxifier.mxcache;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 8:47:53
 * </p>
 * Тип отслеживания зависимостей
 */
public enum DependencyTracking {
    /** По умолчанию (не перекрывает значение) */
    DEFAULT,
    /** Нет отслеживания */
    NONE,
    /** Статическое */
    STATIC,
    /** Динамическое */
    INSTANCE
}
