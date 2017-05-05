/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

import com.maxifier.mxcache.asm.Type;

import static com.maxifier.mxcache.asm.Type.ARRAY;

/**
 * ArgsWrapping
 *
 * @author Azat Abdulvaliev (azat.abdulvaliev@maxifier.com) (2015-11-24 15:19)
 */
public enum ArgsWrapping {
    /** no arguments */
    EMPTY,
    /** tuple with hashing strategies array as first param in constructor */
    RAW,
    /** Since 2.6.2: tuple with hashing strategies array as first param in constructor */
    TUPLE_HS,
    /** Before 2.6.2 only: tuple of one or more elements, to hashing strategies in constructor */
    TUPLE;

    public static ArgsWrapping of(Type[] types, Type[] argsHashingStrats, boolean features262) {
        switch (types.length) {
            case 0:
                return EMPTY;
            case 1:
                if (features262 && (types[0].getSort() == ARRAY || argsHashingStrats[0] != null)) {
                    // wrap single parameter array to override hashCode and equals
                    return TUPLE_HS;
                } else {
                    return RAW;
                }
            default:
                return features262 ? TUPLE_HS : TUPLE;
        }
    }
}