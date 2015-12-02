/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.asm.Type;

import java.lang.reflect.Method;

import static com.maxifier.mxcache.asm.Type.ARRAY;

/**
 * ArgsWrapping
 *
 * @author Azat Abdulvaliev (azat.abdulvaliev@maxifier.com) (2015-11-24 15:19)
 */
public enum ArgsWrapping {
    EMPTY, // no arguments
    RAW,   // one argument, no need to wrap into tuple
    TUPLE; // tuple of one or more elements

    public static ArgsWrapping of(Type[] types, Type[] argsHashingStrats) {
        switch (types.length) {
            case 0:
                return EMPTY;
            case 1:
                // wrap single parameter array to override hashCode and equals
                return (types[0].getSort() == ARRAY || argsHashingStrats[0] != null) ? TUPLE : RAW;
            default:
                return TUPLE;
        }
    }

    public static ArgsWrapping of(Method method, Type[] argsHashingStrats) {
        Class[] args = method.getParameterTypes();
        switch (args.length) {
            case 0:
                return EMPTY;
            case 1:
                // wrap single parameter array to override hashCode and equals
                return (Type.getType(args[0]).getSort() == ARRAY || argsHashingStrats[0] != null) ? TUPLE : RAW;
            default:
                return TUPLE;
        }
    }
}