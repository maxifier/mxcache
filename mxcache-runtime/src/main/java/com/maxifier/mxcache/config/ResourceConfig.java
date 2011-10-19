package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 14.04.2010
 * Time: 11:10:19
 */
public class ResourceConfig {
    @XmlAttribute(required = true)
    private String name;

    public String getName() {
        return name;
    }
}
