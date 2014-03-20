/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceInfo {
    private final String name;

    public ResourceInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
