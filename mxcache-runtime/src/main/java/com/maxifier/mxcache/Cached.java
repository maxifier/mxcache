package com.maxifier.mxcache;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.METHOD;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 23.01.2010
 * Time: 15:12:46
 * <p/>
 * Copyright (c) 1999-2009 Magenta Corporation Ltd. All Rights Reserved.
 * Magenta Technology proprietary and confidential.
 * Use is subject to license terms.
 *
 * <a href="http://wiki.bequest/index.php/Кэширование">См. Вики</a>
 * @author ELectronic ENgine
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {METHOD})
@Documented
public @interface Cached {
    /**
     * Задаёт имя группы кешей. Несколько кешей в одной группе управляются одним менеджером,
     * зависимости от ресурсов и lock`и у них общие
     *
     * @return имя группы кешей
     */
    String group() default "";

    /**
     * @return name of cache. Can be used by strategies to identify cache (e.g. ehcache). Displayed in JConsole plugin.
     */
    String name() default "";

    /**
     * Набор тегов. Тег - любая строка. Теги могут использоваться для очистки кеша. 
     * @return теги
     */
    String[] tags() default {};

    /**
     * Для метода - этот метод используется в этой активности
     *
     * @return имя активности
     */
    String activity() default "";
}
