/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
