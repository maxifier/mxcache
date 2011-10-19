package com.maxifier.mxcache.provider;

import com.maxifier.mxcache.MxCacheException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 31.05.2010
 * Time: 15:25:52
 */
public class PropertyConvertationException extends MxCacheException {
    public PropertyConvertationException() {
    }

    public PropertyConvertationException(String message) {
        super(message);
    }

    public PropertyConvertationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PropertyConvertationException(Throwable cause) {
        super(cause);
    }
}
