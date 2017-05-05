/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

import com.maxifier.mxcache.DependencyTracking;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class RuleInfo {
    private final String name;

    private final String source;

    private final String[] selectors;

    private final DependencyTracking dependencyTracking;

    private final String strategy;

    public RuleInfo(String name, String source, String[] selectors, DependencyTracking dependencyTracking, String strategy) {
        this.source = source;
        this.selectors = selectors;
        this.name = name;
        this.dependencyTracking = dependencyTracking;
        this.strategy = strategy;
    }

    public String getSource() {
        return source;
    }

    public String[] getSelector() {
        return selectors;
    }

    public String getName() {
        return name;
    }

    public DependencyTracking getDependencyTracking() {
        return dependencyTracking;
    }

    public String getStrategy() {
        return strategy;
    }
}
