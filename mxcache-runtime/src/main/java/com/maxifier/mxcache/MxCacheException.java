package com.maxifier.mxcache;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.02.11
 * Time: 9:46
 */
public class MxCacheException extends RuntimeException {
    public MxCacheException() {
    }

    public MxCacheException(String message) {
        super(message);
    }

    public MxCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    public MxCacheException(Throwable cause) {
        super(cause);
    }
}
