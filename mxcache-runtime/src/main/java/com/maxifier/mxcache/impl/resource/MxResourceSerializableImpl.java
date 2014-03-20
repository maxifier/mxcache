/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.impl.resource;

import com.maxifier.mxcache.resource.MxResource;

import java.io.Serializable;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
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
