package com.maxifier.mxcache.instrumentation;

import org.testng.annotations.Test;

import static org.testng.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: kochurov
 * Date: 11.04.12
 * Time: 14:31
 */
@Test
public class InstrumentatorProviderUTest {
    public void testSet() {
        assertNotNull(InstrumentatorProvider.getPreferredVersion());
    }
}
