/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.config;

import com.maxifier.mxcache.DependencyTracking;
import com.maxifier.mxcache.Strategy;
import com.maxifier.mxcache.provider.CachingStrategy;
import com.maxifier.mxcache.resource.ResourceDependency;
import com.maxifier.mxcache.resource.TrackDependency;
import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class JaxbRule implements Rule {
    private static class PropertyValueImpl {
        boolean important;

        Object value;
    }

    private static final Logger logger = LoggerFactory.getLogger(JaxbRule.class);

    @XmlAttribute
    private boolean disabled;

    @XmlElement
    private Boolean disableCache;

    @XmlTransient
    private boolean disableCacheImportant;

    @XmlAttribute
    private boolean important;

    @XmlElement
    private DependencyTracking trackDependency = DependencyTracking.DEFAULT;

    @XmlTransient
    private boolean trackImportant;

    @XmlElement(name = "resourceDependency")
    private Set<String> resourceDependencies;

    @XmlElement(name = "strategy")
    private String strategyClassName;

    @XmlElement(name = "cacheName")
    private String cacheName;

    @XmlTransient
    private boolean cacheNameImportant;

    @XmlTransient
    private Class<? extends CachingStrategy> strategy;

    @XmlTransient
    private boolean strategyImportant;

    @SuppressWarnings ({ "MismatchedQueryAndUpdateOfCollection" })
    @XmlElement (name = "property")
    private List<StrategyProperty> properties;

    @XmlTransient
    private final Set<String> ruleNames = new THashSet<String>();

    @XmlTransient
    private final Map<String, PropertyValueImpl> propertyMap = new THashMap<String, PropertyValueImpl>();

    @Override
    public void override(Method method, String cacheName) {
        if (!cacheNameImportant && cacheName != null) {
            this.cacheName = cacheName;
        }
        if (!strategyImportant) {
            Strategy annotatedStrategy = method.getAnnotation(Strategy.class);
            if (annotatedStrategy != null) {
                strategy = annotatedStrategy.value();
            }
        }
        if (!trackImportant) {
            TrackDependency trackDependency = method.getAnnotation(TrackDependency.class);
            if (trackDependency != null) {
                this.trackDependency = trackDependency.value();
            }
        }
        ResourceDependency dependency = method.getAnnotation(ResourceDependency.class);
        if (dependency != null) {
            if (resourceDependencies == null) {
                resourceDependencies = new THashSet<String>();
            }
            Collections.addAll(resourceDependencies, dependency.value());
        }
    }

    void override(JaxbRule rule) {
        overrideTrackDependency(rule);
        overrideDisableCache(rule);
        overrideStrategyClassName(rule);
        overrideCacheName(rule);
        overrideResourceDependencies(rule);
        overrideProperties(rule);
    }

    private void overrideResourceDependencies(JaxbRule rule) {
        if (rule.resourceDependencies != null) {
            if (resourceDependencies == null) {
                resourceDependencies = rule.resourceDependencies;
            } else {
                resourceDependencies.addAll(rule.resourceDependencies);
            }
        }
    }

    private void overrideTrackDependency(JaxbRule rule) {
        if (rule.trackDependency != DependencyTracking.DEFAULT && (!trackImportant || rule.important)) {
            trackDependency = rule.trackDependency;
            trackImportant = rule.important;
        }
    }

    private void overrideCacheName(JaxbRule rule) {
        if (rule.cacheName != null && (!cacheNameImportant || rule.important)) {
            cacheName = rule.cacheName;
            cacheNameImportant = rule.important;
        }
    }

    private void overrideStrategyClassName(JaxbRule rule) {
        if (rule.strategyClassName != null && (!strategyImportant || rule.important)) {
            strategyClassName = rule.strategyClassName;
            strategyImportant = rule.important;
        }
    }

    private void overrideDisableCache(JaxbRule rule) {
        if (rule.disableCache != null && (!disableCacheImportant || rule.important)) {
            disableCache = rule.disableCache;
            disableCacheImportant = rule.important;
        }
    }

    private void overrideProperties(JaxbRule rule) {
        if (rule.properties != null) {
            for (StrategyProperty property : rule.properties) {
                String name = property.getName();
                PropertyValueImpl oldProperty = propertyMap.get(name);

                boolean set;
                Object value = getValue(rule, property);
                if (oldProperty == null) {
                    oldProperty = new PropertyValueImpl();
                    propertyMap.put(name, oldProperty);
                    set = true;
                } else {
                    set = !oldProperty.important || rule.important;
                }
                if (set) {
                    oldProperty.important = rule.important;
                    oldProperty.value = value;
                }
            }
        }
    }

    private Object getValue(JaxbRule rule, StrategyProperty property) {
        List<String> valueList = property.getValues();
        String stringValue = property.getValue();
        if (valueList != null) {
            if (stringValue != null) {
                valueList = new ArrayList<String>(valueList);
                valueList.add(stringValue);
                logger.error("Invalid configuration for rule " + rule + ": value and values both exist " + property.getName());
            }
            return valueList;
        }
        if (stringValue != null) {
            return stringValue;
        }
        logger.error("Invalid configuration for rule " + rule + ": empty property " + property.getName());
        return null;
    }

    @Override
    public Object getProperty(String name) {
        PropertyValueImpl value = propertyMap.get(name);
        return value == null ? null : value.value;
    }

    @Override
    public boolean isImportantProperty(String name) {
        PropertyValueImpl value = propertyMap.get(name);
        return value != null && value.important;
    }

    @Override
    public DependencyTracking getTrackDependency() {
        return trackDependency;
    }

    @Override
    public Set<String> getResourceDependencies() {
        return resourceDependencies == null ? Collections.<String>emptySet() : resourceDependencies;
    }

    @Override
    public Set<String> getRuleNames() {
        return ruleNames;
    }

    void addRuleName(String name) {
        ruleNames.add(name);
    }

    @Override
    public Class<? extends CachingStrategy> getStrategy() {
        if (strategy != null) {
            return strategy;
        }
        if (strategyClassName == null) {
            return null;
        }
        try {
            //noinspection unchecked
            return (Class<? extends CachingStrategy>) Class.forName(strategyClassName);
        } catch (ClassNotFoundException c) {
            logger.error("Invalid configuration: unknown strategy class " + strategyClassName);
            return null;
        }
    }

    @Override
    public String getCacheName() {
        return cacheName;
    }

    @Override
    public boolean getDisableCache() {
        return disableCache != null && disableCache;
    }

    public String getName() {
        return getRuleNames().toString();
    }

    @Override
    public String toString() {
        return "rule:" + getName();
    }

    public boolean isDisabled() {
        return disabled;
    }
}
