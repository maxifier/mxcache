/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package org.jetbrains.jps.incremental.instrumentation;

import com.intellij.compiler.instrumentation.InstrumentationClassFinder;
import org.jetbrains.jps.ModuleChunk;
import org.jetbrains.jps.incremental.BuilderCategory;
import org.jetbrains.jps.incremental.CompileContext;
import org.jetbrains.jps.incremental.ModuleLevelBuilder;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class ClassProcessingBuilder extends ModuleLevelBuilder {
     public ClassProcessingBuilder(BuilderCategory p1){}

     protected abstract String getProgressMessage();

     protected abstract ModuleLevelBuilder.ExitCode performBuild(CompileContext p1, ModuleChunk p2, InstrumentationClassFinder p3, ModuleLevelBuilder.OutputConsumer p4);

     protected abstract boolean isEnabled(CompileContext p1, ModuleChunk p2);

}
