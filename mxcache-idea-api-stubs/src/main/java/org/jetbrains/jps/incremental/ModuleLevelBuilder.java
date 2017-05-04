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
     public static interface OutputConsumer {
          void registerCompiledClass(BuildTarget<?> p1, CompiledClass p2) throws IOException;

          Map<String, CompiledClass> getCompiledClasses();

     }
     public static enum ExitCode {
          NOTHING_DONE, OK, ABORT, ADDITIONAL_PASS_REQUIRED, CHUNK_REBUILD_REQUIRED
     }
}
