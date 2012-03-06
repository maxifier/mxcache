package com.maxifier.mxcache;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 09.08.2010
 * Time: 12:39:07
 */
public final class MxCache {
    private MxCache() {}

    private static final String VERSION = "2.2.9";

    private static final Version PROJECT_VERSION = loadVersion();

    private static Version loadVersion() {
        String packageVersion = MxCache.class.getPackage().getImplementationVersion();
        if (packageVersion != null) {
            return new Version(packageVersion);
        }
        return loadVersionFromFile();
    }

    private static Version loadVersionFromFile() {
        try {
            InputStream in = MxCache.class.getResourceAsStream("mxcache-version");
            try {
                return new Version(IOUtils.toString(in));
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException e) {
            return Version.UNKNOWN;
        }
    }

    /**
     * @return minimal runtime version compatible with current instrumentation
     */
    public static String getCompatibleVersion() {
        return VERSION;
    }

    /**
     * @return Current version of runtime
     */
    public static Version getVersion() {
        return PROJECT_VERSION;
    }
    
    public static String getVersionString() {
        return PROJECT_VERSION.toString();
    }
    
    public static class Version implements Comparable<Version> {
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
                        // да, тут инвертированный порядок - более короткая версия всегда старше, e.g. 2.2.3-SNAPSHOT меньше  2.2.3
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

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
}
