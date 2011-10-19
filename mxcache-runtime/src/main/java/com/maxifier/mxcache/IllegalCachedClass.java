package com.maxifier.mxcache;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 24.03.2010
 * Time: 11:47:49
 */
public class IllegalCachedClass extends MxCacheException {
    private final String sourceFileName;

    public IllegalCachedClass(String message, String sourceFileName) {
        super(message);
        this.sourceFileName = sourceFileName;
    }

    public IllegalCachedClass(String message, Throwable cause, String sourceFileName) {
        super(message, cause);
        this.sourceFileName = sourceFileName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }
}
