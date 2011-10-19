package com.maxifier.mxcache;

import com.maxifier.mxcache.provider.CachingStrategy;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 22.04.2010
 * Time: 18:54:32
 */
@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target (ElementType.METHOD)
public @interface StatisticsMode {
    StatisticsModeEnum value();
}
