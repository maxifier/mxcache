package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 30.04.2010
 * Time: 19:24:23
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
