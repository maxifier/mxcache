package com.maxifier.mxcache;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 18.10.2010
 * Time: 11:35:39
 */
public class NoSuchInstanceException extends MxCacheException {
    public NoSuchInstanceException() {
    }

    public NoSuchInstanceException(String message) {
        super(message);
    }

    public NoSuchInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoSuchInstanceException(Throwable cause) {
        super(cause);
    }
}
