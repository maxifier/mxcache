/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class IllegalCachedClass extends RuntimeException {
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
