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

    private static final String PROJECT_VERSION = loadVersion();

    private static String loadVersion() {
        String packageVersion = MxCache.class.getPackage().getImplementationVersion();
        if (packageVersion != null) {
            return packageVersion;
        }
        return loadVersionFromFile();
    }

    private static String loadVersionFromFile() {
        try {
            InputStream in = MxCache.class.getResourceAsStream("mxcache-version");
            try {
                return IOUtils.toString(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException e) {
            return "<unknown, " + VERSION + " compatible>";
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
    public static String getVersion() {
        return PROJECT_VERSION;
    }
}
