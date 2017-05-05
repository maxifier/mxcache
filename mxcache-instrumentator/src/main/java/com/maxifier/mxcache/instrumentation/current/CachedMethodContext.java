/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation.current;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
class CachedMethodContext {
    private String name;
    private String group;
    private final List<String> tags = new ArrayList<String>();

    public void setName(String name) {
        this.name = name;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getGroup() {
        return group;
    }

    public List<String> getTags() {
        return tags;
    }
}
