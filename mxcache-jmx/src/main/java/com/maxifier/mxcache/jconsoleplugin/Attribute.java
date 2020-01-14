/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jconsoleplugin;

import javax.management.openmbean.CompositeData;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
*/
@SuppressWarnings("UnusedDeclaration")
public enum Attribute {
    CONTEXT("Context", "context", false, false),
    OWNER("Owner", "owner", true, true, null) {
        @Override
        public String transform(Object o) {
            return o.toString();
        }
    },
    ID("ID", "id", false, false),
    METHOD("Method", "method", true, true, null) {
        @Override
        public String transform(Object o) {
            String s = o.toString();
            return s.substring(s.lastIndexOf('.', s.indexOf('(')) + 1);
        }
    },
    NAME("Name", "name", false, false),
    GROUP("Group", "group", true, false, null),
    TAGS("Tags", "tags", true, false, null) {
        @Override
        public String transform(Object o) {
            StringBuilder b = new StringBuilder();
            boolean f = true;
            for (String s : (String[]) o) {
                if (!s.startsWith("@")) {
                    if (f) {
                        f = false;
                    } else {
                        b.append(", ");
                    }
                    b.append(s);
                }
            }
            return b.toString();
        }
    },
    ANNOTATIONS("Annotations", "tags", true, true, null) {
        @Override
        public String transform(Object o) {
            StringBuilder b = new StringBuilder();
            boolean f = true;
            for (String s : (String[]) o) {
                if (s.startsWith("@")) {
                    if (f) {
                        f = false;
                    } else {
                        b.append(", ");
                    }
                    b.append(s.substring(1));
                }
            }
            return b.toString();
        }
    },
    INSTANCES("Instances", "count", false, false),
    ELEMENTS("Elements", "total", false, false),
    IMPLEMENTATION("Impl", "implementation", true, true, null),

    TOTAL_HITS("Hits", "totalHits", false, false),
    TOTAL_MISSES("Misses", "totalMisses", false, false),
    AVERAGE_CALCULATION("Avg. calculation", "averageCalculation", false, false, new ComparableComparator()) {
        @Override
        public Object transform(Object o) {
            return new Time((Double)o);
        }
    },
    MISS_RATE("Miss rate, %", "missRate", false, false, new ComparableComparator()) {
        @Override
        public Object transform(Object o) {
            return new Rate((Double) o);
        }
    },
    MEMORY("Total memory, parrots", "totalMemory", false, false),
    PROFIT("Total profit, seconds", "totalProfit", false, false);

    private static final Pattern SHORTCUT_PATTERN = Pattern.compile("([\\w_$][\\w\\d_$]*\\.)*([\\w_$][\\w\\d_$]*)([^\\.])");
    private static final int NS_IN_SECOND = 1000000000;
    private static final int MAX_GET_FROM_CACHE_TIME_NS = 2000;

    public static String shortcutClassNames(Object s) {
        return s == null ? "" : SHORTCUT_PATTERN.matcher(s.toString()).replaceAll("$2$3");
    }

    private final String name;

    private final String key;

    private final boolean searchable;

    private final boolean shortcutable;

    private final Comparator comparator;

    Attribute(String name, String key, boolean searchable, boolean shortcutable) {
        this(name, key, searchable, shortcutable, new ComparableComparator());
    }

    Attribute(String name, String key, boolean searchable, boolean shortcutable, Comparator comparator) {
        this.name = name;
        this.key = key;
        this.searchable = searchable;
        this.shortcutable = shortcutable;
        this.comparator = comparator;
    }

    public Object transform(Object o) {
        return o;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public Comparator getComparator() {
        return comparator;
    }

    public Object getValueFromCache(CompositeData cache) {
        if (this == PROFIT) {
            Integer totalHits = (Integer) TOTAL_HITS.getValueFromCache(cache);
            double perHintProfitNs = ((Time) AVERAGE_CALCULATION.getValueFromCache(cache)).getValue() - MAX_GET_FROM_CACHE_TIME_NS;
            return Math.round(totalHits * perHintProfitNs / NS_IN_SECOND);
        } else if (this == MEMORY) {
            Integer instanceCount = (Integer) INSTANCES.getValueFromCache(cache);
            Integer elementCount = (Integer) ELEMENTS.getValueFromCache(cache);
            return instanceCount * 2 + elementCount; // in parrots!
        } else {
            Object value = cache.get(key);
            value = value == null ? "" : transform(value);
            // todo add posibility to switch shortcutting off
            return shortcutable ? shortcutClassNames(value) : value;
        }
    }

}
