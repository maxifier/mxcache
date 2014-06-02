/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.provider;

import gnu.trove.map.hash.THashMap;

import java.util.Map;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
class PropertyOverrides {
    private final PropertyOverrides parent;
    private final Map<StrategyProperty, Object> values;

    PropertyOverrides(PropertyOverrides parent) {
        this.parent = parent;
        values = new THashMap<StrategyProperty, Object>();
    }

    @SuppressWarnings("unchecked")
    public <T> T get(StrategyProperty<T> property) {
        Object value = values.get(property);
        if (value == null && parent != null) {
            return parent.get(property);
        }
        return (T)value;
    }

    public <T> PropertyOverrides override(StrategyProperty<T> property, T value) {
        values.put(property, value);
        return this;
    }
}
