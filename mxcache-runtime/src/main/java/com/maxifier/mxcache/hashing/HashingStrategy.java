package com.maxifier.mxcache.hashing;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 31.05.2010
 * Time: 16:18:45
 */
@Documented
@Target (ElementType.PARAMETER)
@Retention (RetentionPolicy.RUNTIME)
public @interface HashingStrategy {
    Class value();
}
