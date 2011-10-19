package com.maxifier.mxcache.transform;

import com.maxifier.mxcache.MxCacheException;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 11.10.2010
* Time: 10:09:31
*/
public class InvalidTransformAnnotations extends MxCacheException {
    public InvalidTransformAnnotations(String message) {
        super(message);
    }

    public InvalidTransformAnnotations(String message, Throwable cause) {
        super(message, cause);
    }
}
