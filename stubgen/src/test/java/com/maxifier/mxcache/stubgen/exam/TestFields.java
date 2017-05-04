/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.exam;

import com.maxifier.mxcache.stubgen.lib.ClassWithFields;

/**
 * TestFields
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 11:03)
 */
public class TestFields {
    public String testIt() {
        return ClassWithFields.A + " " + ClassWithFields.B + " " + new ClassWithFields().c;
    }
}
