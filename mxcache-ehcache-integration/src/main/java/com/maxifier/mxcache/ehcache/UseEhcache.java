package com.maxifier.mxcache.ehcache;

import com.maxifier.mxcache.EvictionPolicyEnum;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.03.11
 * Time: 18:22
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseEhcache {
    int maxElements();

    EvictionPolicyEnum memoryEvictionPolicy() default EvictionPolicyEnum.DEFAULT;
}
