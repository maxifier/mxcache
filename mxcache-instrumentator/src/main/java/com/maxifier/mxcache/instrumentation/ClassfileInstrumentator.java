/*
 * Copyright (c) 2008-2017 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.instrumentation;

import com.maxifier.mxcache.MxCache;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Artyom Norin (artem.norin@maxifier.com)
 */
public class ClassfileInstrumentator {
    private static final Instrumentator INSTRUMENTATOR = InstrumentatorProvider.getPreferredVersion();
    private static final int MIN_FILES_PER_THREAD = 100;
    private static final int MAX_THREADS_PER_CORE = 2;

    public static String instrumentClasses(File path) throws Exception {
        long start = System.currentTimeMillis();
        File[] files = getAllClasses(path);

        int executorThreads = Math.max(Math.min(Runtime.getRuntime().availableProcessors() * MAX_THREADS_PER_CORE, files.length / MIN_FILES_PER_THREAD), 1);
        int instrumented = instrumentAll(files, executorThreads);

        long end = System.currentTimeMillis();
        return "MxCache instrumentator " + INSTRUMENTATOR + " examined " + files.length + " classes, instrumented " + instrumented + " classes in " + (end - start) + " ms [" + MxCache.getVersionObject() + ", " + executorThreads + " threads]";
    }

    private static int instrumentAll(final File[] files, int executorThreads) throws Exception {
        assert executorThreads > 0;
        if (executorThreads == 1) {
            instrumentAllSingleThread(files);
        }
        return instrumentAllParallel(files, executorThreads);
    }

    private static int instrumentAllParallel(File[] files, int executorThreads) throws Exception {
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
            throw new Exception("Instrumentation interrupted", e);
        }
        Collection<Exception> exceptions = r.getExceptions();
        if (!exceptions.isEmpty()) {
            throw new Exception(exceptions.toString(), exceptions.iterator().next());
        }
        return r.getInstrumented().get();
    }

    private static int instrumentAllSingleThread(File[] f) throws Exception {
        int instrumented = 0;
        for (File file : f) {
            if (instrument(file)) {
                instrumented++;
            }
        }
        return instrumented;
    }

    private static boolean instrument(File file) throws Exception {
        try {
            byte[] bytecode = FileUtils.readFileToByteArray(file);
            ClassInstrumentationResult result = INSTRUMENTATOR.instrument(bytecode);
            if (result != null) {
                writeInstrumentationResults(file, result);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new Exception("Cannot process @Cached", e);
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
