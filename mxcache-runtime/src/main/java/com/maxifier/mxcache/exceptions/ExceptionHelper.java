/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.exceptions;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public final class ExceptionHelper {
    private ExceptionHelper() {}

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwCheckedExceptionHack0(Throwable exception) throws T {
        throw (T) exception;
    }

    /**
     * This hack allows to throw a checked exception without declaring it in throws.
     * @param t an exception
     */
    public static void throwCheckedExceptionHack(Throwable t) {
        ExceptionHelper.<RuntimeException>throwCheckedExceptionHack0(t);
    }

    /**
     * If given value represents an {@link com.maxifier.mxcache.exceptions.ExceptionRecord} and the record is not
     * expired, this method will rethrow it immediately. Otherwise it will do nothing.
     *
     * @param value cache value
     */
    public static void throwIfExceptionRecordNotExpired(Object value) {
        if (value instanceof ExceptionRecord) {
            ExceptionRecord exceptionRecord = (ExceptionRecord) value;
            long expirationTime = exceptionRecord.getExpirationTime();
            if (expirationTime == 0 || expirationTime < System.currentTimeMillis()) {
                throwCheckedExceptionHack(exceptionRecord.getException());
            }
        }
    }
}
