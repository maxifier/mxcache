package com.maxifier.mxcache;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;

/**
 * Project: Maxifier
 * Created by: Yakoushin Andrey
 * Date: 23.01.2010
 * Time: 15:12:46
 * <p/>
 * Ётой аннотацией помечаютс€ методы публичного API. —мело добавл€йте эту аннотацию в список исключений дл€
 * неиспользуемых методов.
 */
@Retention(value = RetentionPolicy.SOURCE)
@Target(value = {METHOD, TYPE, CONSTRUCTOR, FIELD, PARAMETER})
@Documented
public @interface PublicAPI {
}
