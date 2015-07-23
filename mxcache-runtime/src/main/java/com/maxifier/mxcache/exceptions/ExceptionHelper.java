/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
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
}
