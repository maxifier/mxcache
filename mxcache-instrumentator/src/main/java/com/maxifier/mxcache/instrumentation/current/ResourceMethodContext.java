package com.maxifier.mxcache.instrumentation.current;

import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 11.03.11
 * Time: 10:57
 */
public class ResourceMethodContext {
    private final Set<String> readResourceFields = new THashSet<String>();
    private final Set<String> writeResourceFields = new THashSet<String>();

    private final List<String> readResourceOrder = new ArrayList<String>();
    private final List<String> writeResourceOrder = new ArrayList<String>();

    public Set<String> getReadResourceFields() {
        return readResourceFields;
    }

    public Set<String> getWriteResourceFields() {
        return writeResourceFields;
    }

    public List<String> getReadResourceOrder() {
        return readResourceOrder;
    }

    public List<String> getWriteResourceOrder() {
        return writeResourceOrder;
    }
}
