/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

import java.io.FilterInputStream;
import java.io.InputStream;

/**
 * TestInputStream
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-09 18:18)
 */
public class TestInputStream extends FilterInputStream {
    public TestInputStream(InputStream in) {
        super(in);
    }
}
