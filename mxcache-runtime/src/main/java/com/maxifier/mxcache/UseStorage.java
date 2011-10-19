package com.maxifier.mxcache;

import com.maxifier.mxcache.storage.Storage;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.11.2010
 * Time: 9:47:57
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UseStorage {
    Class<? extends Storage> value();
}
