/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.util;

import java.lang.reflect.InvocationTargetException;

/**
 * ExceptionHelper
 *
 * @author Aleksey Dergunov (aleksey.dergunov@maxifier.com) (06.09.13 13:08)
 */
public class ExceptionHelper {
    private ExceptionHelper() {}

    public static <T> T rethrowInvocationTargetException(InvocationTargetException e) {
        if (e.getTargetException() instanceof Error) {
            throw (Error) e.getTargetException();
        }
        if (e.getTargetException() instanceof RuntimeException) {
            throw (RuntimeException) e.getTargetException();
        }
        throw new RuntimeException(e);
    }
}
