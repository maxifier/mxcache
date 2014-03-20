/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@Test
public class InstrumentatorProviderUTest {
    public void testSet() {
        assertNotNull(InstrumentatorProvider.getPreferredVersion());
    }
}
