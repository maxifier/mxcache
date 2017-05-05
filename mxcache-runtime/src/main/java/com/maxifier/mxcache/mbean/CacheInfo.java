/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.mbean;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class CacheInfo {
    private final String context;

    private final String owner;

    private final String declaringClass;

    private final String implementation;

    private final String keyType;
    
    private final String valueType;

    private final String method;

    private final String name;

    private final int id;

    private final int count;

    private final int total;

    private final String group;

    private final String[] tags;

    private final int totalHits;

    private final int totalMisses;

    private final double averageCalculation;

    public CacheInfo(String context, String keyType, String valueType, String method, String name, int id, int count, int total, String group, String[] tags, String implementation, String owner, String declaringClass, int totalHits, int totalMisses, double averageCalculation) {
        this.keyType = keyType;
        this.valueType = valueType;
        this.method = method;
        this.name = name;
        this.id = id;
        this.count = count;
        this.total = total;
        this.group = group;
        this.tags = tags;
        this.implementation = implementation;
        this.owner = owner;
        this.declaringClass = declaringClass;
        this.totalHits = totalHits;
        this.totalMisses = totalMisses;
        this.averageCalculation = averageCalculation;
        this.context = context;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public String getKeyType() {
        return keyType;
    }

    public String getValueType() {
        return valueType;
    }

    public String getOwner() {
        return owner;
    }

    public String getMethod() {
        return method;
    }

    public int getId() {
        return id;
    }

    public int getCount() {
        return count;
    }

    public int getTotal() {
        return total;
    }

    public String getGroup() {
        return group;
    }

    public String[] getTags() {
        return tags;
    }

    public String getImplementation() {
        return implementation;
    }

    public int getTotalHits() {
        return totalHits;
    }

    public int getTotalMisses() {
        return totalMisses;
    }

    public double getAverageCalculation() {
        return averageCalculation;
    }

    public double getMissRate() {
        return ((double)totalMisses) / (totalHits + totalMisses);
    }

    public String getContext() {
        return context;
    }

    public String getName() {
        return name;
    }
}
