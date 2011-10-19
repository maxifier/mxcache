package com.maxifier.mxcache.instrumentation.current;

import java.util.ArrayList;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: dalex
* Date: 19.10.2010
* Time: 12:50:42
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
