/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jconsoleplugin;

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
    };

    private static final Pattern SHORTCUT_PATTERN = Pattern.compile("([\\w_$][\\w\\d_$]*\\.)*([\\w_$][\\w\\d_$]*)([^\\.])");

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

    public String getKey() {
        return key;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isShortcutable() {
        return shortcutable;
    }

    public Comparator getComparator() {
        return comparator;
    }
}
