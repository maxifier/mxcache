/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jpsplugin;

import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.maxifier.mxcache.MxCache;
import com.maxifier.mxcache.Version;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.ClassInstrumentationResult;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;
import org.apache.commons.io.IOUtils;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.ProjectPaths;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.instrumentation.ClassProcessingBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * MxCacheBuildInstrumentator - just a basic instrumentator is not enough for use as we also generate Calculable and
 * Cleanable classes on-the-fly.
 *
 * @author Alexander Kochurov (alexander.kochurov@maxifier.com) (2014-03-14 17:28)
 */
public class MxCacheBuildInstrumentator extends ClassProcessingBuilder {
    private static final Logger logger = Logger.getInstance(MxCacheBuildInstrumentator.class);

    private static final Key<Boolean> IS_INSTRUMENTED_KEY = Key.create("mxcache_instrumentation_marker");

    public MxCacheBuildInstrumentator() {
        super(BuilderCategory.CLASS_INSTRUMENTER);
    }

    @Nonnull
    @Override
    public String getPresentableName() {
        return "MxCache instrumentator";
    }

    @Override
    protected boolean isEnabled(CompileContext context, ModuleChunk chunk) {
        return true;
    }

    @Nullable
    private Version findRuntimeVersion(CompileContext context, ModuleChunk chunk) {
        for (File f : ProjectPaths.getCompilationClasspathFiles(chunk, false, false, false)) {
            if (f.getName().matches("mxcache-runtime.*\\.jar")) {
                try {
                    JarFile file = new JarFile(f);
                    ZipEntry entry = file.getEntry("com/maxifier/mxcache/mxcache-version");
                    if (entry != null) {
                        BufferedReader br = null;
                        try {
                            br = new BufferedReader(new InputStreamReader(file.getInputStream(entry)));
                            String runtimeVersion = br.readLine();
                            context.processMessage(new CompilerMessage(
                                    getPresentableName(),
                                    BuildMessage.Kind.INFO,
                                    "Detected MxCache runtime version: " + runtimeVersion,
                                    f.getPath()));
                            if (runtimeVersion.contains("-SNAPSHOT")) {
                                runtimeVersion = runtimeVersion.substring(0, runtimeVersion.length() - "-SNAPSHOT".length());
                            }
                            return new Version(runtimeVersion);
                        } finally {
                            IOUtils.closeQuietly(br);
                        }
                    }
                } catch (IOException e) {
                    context.processMessage(new CompilerMessage(getPresentableName(), e));
                }
            }
        }
        return null;
    }

    @Nonnull
    private Version resolveInstrumentatorVersion(CompileContext context, ModuleChunk chunk) {
        Version runtimeVersion = findRuntimeVersion(context, chunk);
        if (runtimeVersion == null) {
            return new Version(MxCache.getCompatibleVersion());
        } else {
            TreeSet<Version> versions = new TreeSet<Version>();
            for (String instrVersions : InstrumentatorProvider.getAvailableVersions().keySet()) {
                versions.add(new Version(instrVersions));
            }
            Version result = versions.floor(runtimeVersion);
            if (result == null) {
                return new Version(MxCache.getCompatibleVersion());
            }
            return result;
        }
    }

    @Override
    protected ExitCode performBuild(CompileContext context, ModuleChunk chunk, InstrumentationClassFinder finder, OutputConsumer outputConsumer) {
        Version instVersion = resolveInstrumentatorVersion(context, chunk);

        context.processMessage(new CompilerMessage(
                getPresentableName(),
                BuildMessage.Kind.INFO,
                "Using MxCache instrumentation version: " + instVersion,
                null));

        ExitCode exitCode = ExitCode.NOTHING_DONE;
        for (CompiledClass compiledClass : getCompiledClassesSnapshot(outputConsumer)) {
            if (!IS_INSTRUMENTED_KEY.get(compiledClass, Boolean.FALSE)) {
                try {
                    Instrumentator instrumentator = InstrumentatorProvider.getExactVersion(instVersion.toString());
                    ClassInstrumentationResult res = instrumentator.instrument(compiledClass.getContent().toByteArray());
                    if (res != null) {
                        compiledClass.setContent(new BinaryContent(res.getInstrumentedBytecode()));
                        IS_INSTRUMENTED_KEY.set(compiledClass, Boolean.TRUE);
                        finder.cleanCachedData(compiledClass.getClassName());
                        for (ClassDefinition additionalClass : res.getAdditionalClasses()) {
                            CompiledClass newClass = new CompiledClass(
                                    getSiblingFile(compiledClass.getOutputFile(), additionalClass.getName()),
                                    compiledClass.getSourceFile(),
                                    additionalClass.getName(),
                                    new BinaryContent(additionalClass.getBytecode())
                            );
                            newClass.save();
                            outputConsumer.registerCompiledClass(chunk.representativeTarget(), newClass);
                            // clean cache because this class may have already existed before
                            finder.cleanCachedData(additionalClass.getName());
                            IS_INSTRUMENTED_KEY.set(newClass, Boolean.TRUE);
                        }
                        exitCode = ExitCode.OK;
                    }
                } catch (Throwable e) {
                    logger.info(e);
                    String message = e.getMessage();
                    context.processMessage(new CompilerMessage(getPresentableName(), BuildMessage.Kind.ERROR, message == null ? e.getClass().getName() : message, compiledClass.getSourceFile().getPath()));
                }
            }
        }
        return exitCode;
    }

    private static List<CompiledClass> getCompiledClassesSnapshot(OutputConsumer outputConsumer) {
        return new ArrayList<CompiledClass>(outputConsumer.getCompiledClasses().values());
    }

    private static File getSiblingFile(File file, String className) {
        String filename = className.substring(className.lastIndexOf('.') + 1);
        return new File(file.getParentFile(), filename + ".class");
    }

    @Override
    protected String getProgressMessage() {
        return "Instrumenting caches...";
    }
}
