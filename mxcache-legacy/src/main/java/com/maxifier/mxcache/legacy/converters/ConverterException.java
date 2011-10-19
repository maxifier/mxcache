package com.maxifier.mxcache.legacy.converters;

import com.maxifier.mxcache.MxCacheException;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 21.04.2010
 * Time: 16:28:36
 */
public class ConverterException extends MxCacheException {
    public ConverterException() {
    }

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConverterException(Throwable cause) {
        super(cause);
    }
}
