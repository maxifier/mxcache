/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class ResourceConfig {
    @XmlAttribute(required = true)
    private String name;

    public String getName() {
        return name;
    }
}
