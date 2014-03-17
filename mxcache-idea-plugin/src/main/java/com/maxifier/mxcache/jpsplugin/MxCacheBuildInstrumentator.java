/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package com.maxifier.mxcache.jpsplugin;

import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.maxifier.mxcache.instrumentation.ClassDefinition;
import com.maxifier.mxcache.instrumentation.ClassInstrumentationResult;
import com.maxifier.mxcache.instrumentation.Instrumentator;
import com.maxifier.mxcache.instrumentation.InstrumentatorProvider;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.*;
import org.jetbrains.jps.incremental.instrumentation.ClassProcessingBuilder;
import org.jetbrains.jps.incremental.messages.BuildMessage;
import org.jetbrains.jps.incremental.messages.CompilerMessage;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    protected ExitCode performBuild(CompileContext context, ModuleChunk chunk, InstrumentationClassFinder finder, OutputConsumer outputConsumer) {
        ExitCode exitCode = ExitCode.NOTHING_DONE;
        for (CompiledClass compiledClass : getCompiledClassesSnapshot(outputConsumer)) {
            if (!IS_INSTRUMENTED_KEY.get(compiledClass, Boolean.FALSE)) {
                try {
                    Instrumentator instrumentator = InstrumentatorProvider.getPreferredVersion();
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
