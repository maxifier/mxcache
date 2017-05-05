/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.stubgen.lib;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * TestImportInnerClass
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-04-10 09:09)
 */
public class ClassWithEntries {
    public Set<Map.Entry<String, String>> entrySet() {
        return Collections.emptySet();
    }
}
