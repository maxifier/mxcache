package com.maxifier.mxcache.resource;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 12.04.2010
 * Time: 9:21:21
 */
@Documented
@Target(ElementType.METHOD)
@Retention (RetentionPolicy.CLASS)
public @interface ResourceReader {
    /** @return cписок имен ресурсов */
    String[] value();
}
