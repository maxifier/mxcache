/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.exam;

import com.maxifier.mxcache.stubgen.lib.OtherInterface;
import com.maxifier.mxcache.stubgen.lib.ParentInterface;
import com.maxifier.mxcache.stubgen.lib.SomeInterface;
import com.maxifier.mxcache.stubgen.lib.Subclass;

/**
 * TestInheritance
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 16:48)
 */
public class TestInheritance {
    public void testIt() {
        Subclass s = new Subclass();
        ParentInterface i = s;
        i.x();
        s.z();
        OtherInterface o = s;
        o.w("test");
    }
}
