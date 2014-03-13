/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class StrategyProperty {
    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute
    private String value;

    @XmlElement (name = "value")
    private List<String> values;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<String> getValues() {
        return values;
    }
}
