/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.exam;

import com.maxifier.mxcache.stubgen.lib.ExtendedEnum;
import com.maxifier.mxcache.stubgen.lib.MyEnum;

/**
 * TestEnum
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 10:21)
 */
public class TestEnum {
    public MyEnum testIt() {
        return MyEnum.USED_CONSTANT;
    }

    public void testIt2() {
        ExtendedEnum.A.z();
    }
}
