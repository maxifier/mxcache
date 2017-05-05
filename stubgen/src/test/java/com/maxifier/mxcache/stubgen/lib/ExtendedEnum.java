/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

/**
 * ExtendedEnum
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 16:45)
 */
public enum ExtendedEnum {
    A, B, C;

    public String z() {
        return name();
    }
}
