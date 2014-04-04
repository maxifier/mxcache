/*
 * Copyright (c) 2008-2014 Maxifier Ltd. All Rights Reserved.
 */
package org.jetbrains.jps.incremental;

import java.io.IOException;
import java.util.Map;
import org.jetbrains.jps.builders.BuildTarget;

/**
 * THIS CLASS WAS GENERATED AUTOMATICALLY WITH StubGen BASED ON IDEA BINARIES
 * DON'T MODIFY IT MANUALLY!

 * @author Alexander Kochurov (alexander.kochurov@maxifier.com)
 */
public abstract class ModuleLevelBuilder extends Builder {
     public ModuleLevelBuilder() {}
     public static interface OutputConsumer {
          public abstract void registerCompiledClass(BuildTarget<? extends Object> p1, CompiledClass p2) throws IOException;

          public abstract Map<String, CompiledClass> getCompiledClasses();

     }
     public static enum ExitCode {
          NOTHING_DONE, OK;
     }
}
