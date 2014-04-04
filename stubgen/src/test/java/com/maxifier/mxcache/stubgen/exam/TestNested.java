/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.exam;

import com.maxifier.mxcache.stubgen.lib.OuterClass;

/**
 * TestB
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-08 18:55)
 */
public class TestNested {
    void testOuter() {
        OuterClass.NestedClass v = new OuterClass.NestedClass();
        v.x = 3;

        OuterClass.InnerClass v2 = new OuterClass().new InnerClass();
        v2.y = 4;
    }
}
