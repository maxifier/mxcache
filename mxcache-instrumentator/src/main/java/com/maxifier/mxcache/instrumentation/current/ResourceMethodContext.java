package com.maxifier.mxcache.instrumentation.current;

import com.maxifier.mxcache.util.MxField;
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
    private final Set<MxField> readResourceFields = new THashSet<MxField>();
    private final Set<MxField> writeResourceFields = new THashSet<MxField>();

    private final List<MxField> readResourceOrder = new ArrayList<MxField>();
    private final List<MxField> writeResourceOrder = new ArrayList<MxField>();

    public Set<MxField> getReadResourceFields() {
        return readResourceFields;
    }

    public Set<MxField> getWriteResourceFields() {
        return writeResourceFields;
    }

    public List<MxField> getReadResourceOrder() {
        return readResourceOrder;
    }

    public List<MxField> getWriteResourceOrder() {
        return writeResourceOrder;
    }
}
