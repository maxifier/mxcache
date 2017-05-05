/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache;

/**
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public class Version implements Comparable<Version> {
    public static final Version UNKNOWN = new Version("UNKNOWN");

    private final String text;

    public Version(String text) {
        this.text = text;
    }

    @Override
    public int compareTo(Version o) {
        if (this == o) {
            return 0;
        }
        String t1 = text;
        String t2 = o.text;
        int p1 = 0;
        int p2 = 0;

        int n1 = t1.length();
        int n2 = t2.length();

        while (true) {
            if (p1 == n1) {
                if (p2 == n2) {
                    return 0;
                } else {
                    // yes the order is inverted as shorter version is always older,
                    // e.g. 2.2.3-SNAPSHOT is less than  2.2.3
                    return 1;
                }
            } else if (p2 == n2) {
                return -1;
            }
            char c1 = t1.charAt(p1);
            char c2 = t2.charAt(p2);
            if (Character.isDigit(c1) && Character.isDigit(c2)) {
                int e1 = findNumberEnd(t1, p1);
                int e2 = findNumberEnd(t2, p2);
                long v1 = Long.valueOf(t1.substring(p1, e1));
                long v2 = Long.valueOf(t2.substring(p2, e2));

                if (v1 < v2) {
                    return -1;
                } else if (v1 > v2) {
                    return 1;
                }

                p1 = e1;
                p2 = e2;
            } else if (c1 < c2) {
                return -1;
            } else if (c1 > c2) {
                return 1;
            } else {
                p1++;
                p2++;
            }
        }
    }

    private static int findNumberEnd(String text, int pos) {
        int n = text.length();
        while (pos < n && Character.isDigit(text.charAt(pos))) {
            pos++;
        }
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Version version = (Version) o;

        return text.equals(version.text);

    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public String toString() {
        return text;
    }
}
