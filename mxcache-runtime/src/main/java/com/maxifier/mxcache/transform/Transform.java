package com.maxifier.mxcache.transform;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.09.2010
 * Time: 18:57:38
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Transform {
    String ONLY_PUBLIC_METHOD = "<the only public method>";

    Class owner() default KEY_ITSELF.class;

    String method() default ONLY_PUBLIC_METHOD;

    final class KEY_ITSELF {
        private KEY_ITSELF() {
        }
    }
}
