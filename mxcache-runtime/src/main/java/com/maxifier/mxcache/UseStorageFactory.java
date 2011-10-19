package com.maxifier.mxcache;

import com.maxifier.mxcache.provider.StorageFactory;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.11.2010
 * Time: 9:48:08
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseStorageFactory {
    Class<? extends StorageFactory> value();
}
