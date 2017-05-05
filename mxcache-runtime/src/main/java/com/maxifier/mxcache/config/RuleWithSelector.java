/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@XmlRootElement (name = "rule")
class RuleWithSelector extends JaxbRule {
    @XmlAttribute
    private String name;

    @SuppressWarnings ({ "MismatchedQueryAndUpdateOfCollection" })
    @XmlElement (name = "selector")
    private List<Selector> selectors;

    private String source;

    List<Selector> getSelectors() {
        return selectors;
    }

    boolean matches(Class className, String group, String[] tags) {
        if (selectors == null || selectors.isEmpty()) {
            return true;
        }
        for (Selector selector : selectors) {
            if (selector.matches(className, group, tags)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
