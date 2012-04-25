package com.maxifier.mxcache.mavenplugin;

import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;
import com.maxifier.mxcache.instrumentation.ClassInstrumentationResult;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.maxifier.mxcache.instrumentation.ClassDefinition;

/**
 * Created by IntelliJ IDEA.
 * User: dalex
 * Date: 17.03.2010
 * Time: 16:33:58
 */
public abstract class AbstractInstrumentatorMojo extends AbstractInstrumentator {
    private static final Instrumentator INSTRUMENTATOR = InstrumentatorProvider.getPreferredVersion();
    private static final int MIN_FILES_PER_THREAD = 10;
    private static final int MAX_THREADS_PER_CORE = 2;

    protected void instrumentClasses(File path) throws MojoExecutionException {
        long start = System.currentTimeMillis();
        File[] files = getAllClasses(path);

        int executorThreads = Math.max(Math.min(Runtime.getRuntime().availableProcessors() * MAX_THREADS_PER_CORE, files.length / MIN_FILES_PER_THREAD), 1);
        int instrumented = instrumentAll(files, executorThreads);

        long end = System.currentTimeMillis();
        getLog().info("MxCache instrumentator " + INSTRUMENTATOR + " examined " + files.length + " classes, instrumented " + instrumented + " classes in " + (end-start) + " ms [" + MxCache.getVersionObject() + ", " + executorThreads + " threads]");
    }

    private int instrumentAll(final File[] files, int executorThreads) throws MojoExecutionException {
        assert executorThreads > 0;
        if (executorThreads == 1) {
            instrumentAllSingleThread(files);
        }
        return instrumentAllParallel(files, executorThreads);
    }

    private int instrumentAllParallel(File[] files, int executorThreads) throws MojoExecutionException {
        InstrumentationRunnable r = new InstrumentationRunnable(files);
        Thread[] threads = new Thread[executorThreads - 1];
        for (int i = executorThreads - 2; i >= 0; i--) {
            Thread t = new Thread(r, "Instrumentation thread " + i);
            threads[i] = t;
            t.start();
        }
        r.run();
        try {
            for (Thread t : threads) {
                t.join();
            }
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Instrumentation interrupted");
        }
        Collection<Exception> exceptions = r.getExceptions();
        if (!exceptions.isEmpty()) {
            for (Exception exception : exceptions) {
                getLog().error(exception);
            }
            throw new MojoExecutionException(exceptions.toString());
        }
        return r.getInstrumented().get();
    }

    private int instrumentAllSingleThread(File[] f) throws MojoExecutionException {
        int instrumented = 0;
        for (File file : f) {
            if (instrument(file)) {
                instrumented++;
            }
        }
        return instrumented;
    }

    private static boolean instrument(File file) throws MojoExecutionException {
        try {
            byte[] bytecode = FileUtils.readFileToByteArray(file);
            ClassInstrumentationResult result = INSTRUMENTATOR.instrument(bytecode);
            if (result != null) {
                writeInstrumentationResults(file, result);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new MojoExecutionException("Cannot process @Cached", e);
        }
    }

    private static void writeInstrumentationResults(File file, ClassInstrumentationResult result) throws IOException {
        List<ClassDefinition> additionalClasses = result.getAdditionalClasses();
        byte[] instrumentedBytecode = result.getInstrumentedBytecode();
        File dir = file.getParentFile();
        for (ClassDefinition additionalClass : additionalClasses) {
            String className = additionalClass.getName();
            int index = className.lastIndexOf('.');
            String classFileName = className.substring(index + 1) + ".class";
            File f = new File(dir, classFileName);
            FileUtils.writeByteArrayToFile(f, additionalClass.getBytecode());
        }
        FileUtils.writeByteArrayToFile(file, instrumentedBytecode);
    }

    private static File[] getAllClasses(File path) {
        List<File> files = new ArrayList<File>();
        getAllClasses(files, path);
        return files.toArray(new File[files.size()]);
    }

    private static void getAllClasses(List<File> f, File in) {
        File[] children = in.listFiles();
        if (children != null) {
            for (File file : children) {
                if (file.isDirectory()) {
                    getAllClasses(f, file);
                } else if (file.getName().toLowerCase().endsWith(".class")) {
                    f.add(file);
                }
            }
        }
    }

    private static class InstrumentationRunnable implements Runnable {
        private final AtomicInteger current = new AtomicInteger();
        private final AtomicInteger instrumented = new AtomicInteger();
        private final Collection<Exception> exceptions = Collections.synchronizedCollection(new ArrayList<Exception>());
        private final File[] files;

        public InstrumentationRunnable(File[] files) {
            this.files = files;
        }

        @Override
        public void run() {
            int id = current.getAndIncrement();
            while (id < files.length) {
                File file = files[id];
                try {
                    if (instrument(file)) {
                        instrumented.incrementAndGet();
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                }
                id = current.getAndIncrement();
            }
        }

        public AtomicInteger getInstrumented() {
            return instrumented;
        }

        public Collection<Exception> getExceptions() {
            return exceptions;
        }
    }
}
