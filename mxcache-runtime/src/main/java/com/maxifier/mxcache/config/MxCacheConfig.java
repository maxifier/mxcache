/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import gnu.trove.THashSet;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
@XmlRootElement(name = "mxcache")
final class MxCacheConfig {
    @XmlElement(name = "resource")
    private final List<ResourceConfig> resources = new ArrayList<ResourceConfig>();

    @XmlElement(name = "rule")
    private final List<RuleWithSelector> rules = new ArrayList<RuleWithSelector>();

    private final Set<String> sources = new THashSet<String>();

    void setSource(String source) {
        for (RuleWithSelector rule : rules) {
            rule.setSource(source);
        }
    }

    public List<ResourceConfig> getResources() {
        return resources;
    }

    public List<RuleWithSelector> getRules() {
        return rules;
    }

    public void addSource(URL url) {
        sources.add(url.toExternalForm());
    }

    public boolean hasSource(URL url) {
        return sources.contains(url.toExternalForm());
    }

    void merge(MxCacheConfig config) {
        resources.addAll(config.resources);
        sources.addAll(config.sources);
        for (RuleWithSelector rule : config.rules) {
            if (!rule.isDisabled()) {
                rules.add(rule);
            }
        }
    }
}
