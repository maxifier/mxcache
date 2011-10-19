package com.maxifier.mxcache;

import java.lang.annotation.*;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.02.11
 * Time: 9:57
 * <p>
 * This annotation is added to all instrumented classes with @Cached methods
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CachedInstrumented {
    /**
     * @return version of instrumentator
     */
    String version();

    /**
     * @return minimal compatible version of runtime
     */
    String compatibleVersion();
}
