package com.maxifier.mxcache.transform;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 15.09.2010
 * Time: 12:52:20
 */
@Transform(owner = BasicTransforms.class, method = "createWeakReference")
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface WeakKey {
}
