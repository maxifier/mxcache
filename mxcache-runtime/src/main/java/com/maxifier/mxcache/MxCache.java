package com.maxifier.mxcache;

import com.maxifier.mxcache.impl.resource.DependencyNode;
import com.maxifier.mxcache.impl.resource.DependencyTracker;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

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
    public static Version getVersionObject() {
        return PROJECT_VERSION;
    }
    
    public static String getVersion() {
        return PROJECT_VERSION.toString();
    }

    @PublicAPI
    public static boolean probe(Runnable task) {
        DependencyNode prevNode = DependencyTracker.track(DependencyTracker.PROBE_NODE);
        try {
            task.run();
            return true;
        } catch (InternalProbeFailedError e) {
            return false;
        } finally {
            DependencyTracker.exit(prevNode);
        }
    }
    
    @PublicAPI
    public static <T> T probe(Callable<T> task) throws ProbeFailedException {
        DependencyNode prevNode = DependencyTracker.track(DependencyTracker.PROBE_NODE);
        try {
            return task.call();
        } catch (InternalProbeFailedError e) {
            throw new ProbeFailedException(e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            DependencyTracker.exit(prevNode);
        }
    }
}
