package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 20.04.2010
 * Time: 11:18:09
 */
class MxResourceSerializableImpl implements Serializable {
    private static final long serialVersionUID = 100L; 

    private final String name;

    public MxResourceSerializableImpl(MxResource resource) {
        this.name = resource.getName();
    }

    public Object readResolve() {
        return MxResourceFactory.getResource(name);
    }
}
